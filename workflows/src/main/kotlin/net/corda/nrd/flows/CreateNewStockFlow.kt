package net.corda.nrd.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.utilities.withNotary
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.services.IdentityService
import net.corda.core.utilities.ProgressTracker
import net.corda.nrd.states.StockState
import java.math.BigDecimal
import java.util.*

@StartableByRPC
class CreateNewStockFlow(
    val symbol: String,
    val name: String,
    val currency: String,
    val price: BigDecimal,
    val issueVol: Int
) : FlowLogic<UniqueIdentifier>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): UniqueIdentifier {
        val notary = serviceHub.networkMapCache.notaryIdentities.single()
        val operator = serviceHub.networkMapCache.getPeerByLegalName(CordaX500Name.parse("O=NSDOperator,L=Moscow,C=RU"))

        val uuid = UniqueIdentifier()
        val tokenState = StockState(
            issuer = ourIdentity,
            symbol = symbol,
            name = name,
            currency = currency,
            price = price,
            dividend = BigDecimal.ZERO,
            exDate = Date(),
            payDate = Date(),
            linearId = uuid,
            maintainers = listOf(operator!!, ourIdentity),
            issueVol = issueVol.toLong()
        )

        val transactionState = tokenState withNotary notary
        subFlow(CreateEvolvableTokens(transactionState))
        return uuid
    }

}
