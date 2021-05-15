package net.corda.nrd.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.workflows.accountService
import com.r3.corda.lib.tokens.contracts.internal.schemas.FungibleTokenSchema
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.contracts.utilities.heldBy
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.contracts.utilities.of
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import net.corda.core.contracts.Amount
import net.corda.core.flows.*
import net.corda.core.identity.AnonymousParty
import net.corda.core.identity.Party
import net.corda.core.utilities.ProgressTracker
import net.corda.nrd.flows.accountsUtilities.NewKeyForAccount

@StartableByRPC
class FiatCurrencyIssueFlow(
    val currency: String,
    val amount: Long,
    val recipient: String
) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): String {
        val token = FiatCurrency.Companion.getInstance(currency)
        println("1")
        val myAccount = accountService.accountInfo(recipient).single().state.data
        println("2")
        val myKey = subFlow(NewKeyForAccount(myAccount.identifier.id)).owningKey
        println("3")
        val fungibleToken: FungibleToken = amount of token issuedBy ourIdentity heldBy AnonymousParty(myKey)
        println("4")

        val stx = subFlow(IssueTokens(listOf(fungibleToken)))
        return "Issued $amount $currency token(s) to $recipient"
    }
}
