package net.corda.nrd.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class GetAllTokens(
    val status: String
) : FlowLogic<List<StateAndRef<FungibleToken>>>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): List<StateAndRef<FungibleToken>> {
        val inputCriteria = QueryCriteria.LinearStateQueryCriteria(
            status = when (status) {
                "consumed" -> Vault.StateStatus.CONSUMED
                else -> Vault.StateStatus.UNCONSUMED
            }
        )
        return serviceHub.vaultService.queryBy(
            FungibleToken::class.java,
            criteria = inputCriteria
        ).states
    }
}
