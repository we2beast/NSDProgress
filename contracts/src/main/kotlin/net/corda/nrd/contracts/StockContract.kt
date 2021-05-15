package net.corda.nrd.contracts

import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract
import com.r3.corda.lib.tokens.contracts.commands.Create
import com.r3.corda.lib.tokens.contracts.commands.EvolvableTokenTypeCommand
import com.r3.corda.lib.tokens.contracts.commands.Update
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import net.corda.nrd.states.StockState
import java.math.BigDecimal

class StockContract : EvolvableTokenContract(), Contract {

    companion object {
        const val CONTRACT_ID = "net.corda.nrd.contracts.StockContract"
    }

    @Throws(IllegalArgumentException::class)
    override fun verify(tx: LedgerTransaction) {
        val outputState: StockState = tx.getOutput(0) as StockState
        // Dispatch based on command.
        val command = tx.commands.requireSingleCommand<EvolvableTokenTypeCommand>()
        when (command.value) {
            is Create -> additionalCreateChecks(tx)
            is Update -> additionalUpdateChecks(tx)
        }
    }


    override fun additionalCreateChecks(tx: LedgerTransaction) { // Number of outputs is guaranteed as 1
        val createdStockState: StockState = tx.outputsOfType(StockState::class.java)[0]
        requireThat {
            //Validations when creating a new stock
            "Stock symbol must not be empty".using(!createdStockState.symbol.isEmpty())
            "Stock name must not be empty".using(!createdStockState.name.isEmpty())
            "Stock dividend must start with zero".using(createdStockState.dividend.equals(BigDecimal.ZERO))
        }
    }

    override fun additionalUpdateChecks(tx: LedgerTransaction) { // Number of inputs and outputs are guaranteed as 1
        val input: StockState = tx.inputsOfType(StockState::class.java)[0]
        val output: StockState = tx.outputsOfType(StockState::class.java)[0]
        requireThat {
            //Validations when a stock is updated, ie. AnnounceDividend (UpdateEvolvableToken)
            "Stock Symbol must not be changed.".using(input.symbol == output.symbol)
            "Stock Currency must not be changed.".using(input.currency == output.currency)
            "Stock Name must not be changed.".using(input.name == output.name)
            "Stock Company must not be changed.".using(input.issuer == output.issuer)
        }
    }

}
