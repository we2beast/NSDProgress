package net.corda.nrd.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.workflows.utilities.tokenBalance
import net.corda.core.contracts.Amount
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.ProgressTracker
import net.corda.nrd.states.StockState
import java.util.*

@InitiatingFlow
@StartableByRPC
class GetStockTokenBalance(
    val uuid: String
) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): String {
        val inputCriteria = QueryCriteria.LinearStateQueryCriteria(
            uuid = listOf(UUID.fromString(uuid)),
            status = Vault.StateStatus.UNCONSUMED
        )
        val customTokenState = serviceHub.vaultService.queryBy(
            StockState::class.java,
            criteria = inputCriteria
        ).states.single().state.data
        val tokenPointer = customTokenState.toPointer(customTokenState.javaClass)
        val amount: Amount<TokenType> = serviceHub.vaultService.tokenBalance(tokenPointer)
        return amount.quantity.toString()
    }

}
