package net.corda.nrd.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.workflows.accountService
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.contracts.utilities.amount
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveFungibleTokens
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveFungibleTokensHandler
import com.r3.corda.lib.tokens.workflows.types.PartyAndAmount
import com.r3.corda.lib.tokens.workflows.utilities.heldTokenAmountCriteria
import com.r3.corda.lib.tokens.workflows.utilities.tokenAmountWithIssuerCriteria
import net.corda.core.contracts.Amount
import net.corda.core.flows.*
import net.corda.core.identity.AnonymousParty
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.ProgressTracker
import net.corda.nrd.flows.accountsUtilities.NewKeyForAccount
import net.corda.nrd.states.StockState
import java.util.*


@InitiatingFlow
@StartableByRPC
class MoveStockToken(
    val uuid: String,
    val quantity: Long,
    val recipient: String
) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): String {
        val myAccount = accountService.accountInfo(recipient).single().state.data
        val myKey = subFlow(NewKeyForAccount(myAccount.identifier.id)).owningKey
        val inputCriteria = QueryCriteria.LinearStateQueryCriteria(
            uuid = listOf(UUID.fromString(uuid)),
            status = Vault.StateStatus.UNCONSUMED
        )
        val customTokenState = serviceHub.vaultService.queryBy(
            StockState::class.java,
            criteria = inputCriteria
        ).states.single().state.data

        val tokenPointer: TokenPointer<StockState> = customTokenState.toPointer(customTokenState.javaClass)

        val amount: Amount<TokenType> = Amount(quantity, tokenPointer)
        val stx = subFlow(MoveFungibleTokens(amount, AnonymousParty(myKey)))
        return stx.id.toString()
    }

}

@InitiatedBy(MoveStockToken::class)
class MoveStockResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        return subFlow(MoveFungibleTokensHandler(counterpartySession))
    }

}
