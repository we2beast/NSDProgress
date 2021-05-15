package net.corda.nrd.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import net.corda.core.contracts.Amount
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.ProgressTracker
import net.corda.nrd.states.StockState
import java.util.*

@StartableByRPC
class IssueStockToken(
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

        val issuedToken = customTokenState.toPointer(customTokenState.javaClass) issuedBy ourIdentity

        val issueAmount = Amount(customTokenState.issueVol, issuedToken)
        val stockToken = FungibleToken(issueAmount, customTokenState.issuer)

        val stx = subFlow(IssueTokens(listOf(stockToken), listOf(customTokenState.issuer)))

        return stx.id.toString()
    }

}


