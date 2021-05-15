package net.corda.nrd.states

import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.schemas.StatePersistable
import net.corda.nrd.contracts.StockContract
import java.math.BigDecimal
import java.util.*

@BelongsToContract(StockContract::class)
data class StockState(
    val issuer: Party,
    val symbol: String,
    val name: String,
    val currency: String,
    val price: BigDecimal,
    val dividend: BigDecimal,
    val issueVol: Long,
    val exDate: Date,
    val payDate: Date,
    override val linearId: UniqueIdentifier,
    override val fractionDigits: Int = 0,
    override val maintainers: List<Party> = listOf()
) : EvolvableTokenType(), StatePersistable
