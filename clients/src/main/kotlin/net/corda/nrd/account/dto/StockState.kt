package net.corda.nrd.account.dto

import java.math.BigDecimal
import java.util.*

data class StockStateDto(
    val issuer: String,
    val symbol: String,
    val name: String,
    val currency: String,
    val price: BigDecimal,
    val dividend: BigDecimal,
    val issueVol: Long,
    val exDate: Date,
    val payDate: Date,
    val linearId: String
)
