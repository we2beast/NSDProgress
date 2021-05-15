package net.corda.nrd.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.ProgressTracker
import net.corda.nrd.states.StockState

@InitiatingFlow
@StartableByRPC
class GetAllStockToken : FlowLogic<List<StateAndRef<StockState>>>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): List<StateAndRef<StockState>> {
        val inputCriteria = QueryCriteria.LinearStateQueryCriteria(
            status = Vault.StateStatus.UNCONSUMED
        )
        return serviceHub.vaultService.queryBy(
            StockState::class.java,
            criteria = inputCriteria
        ).states
    }

}
