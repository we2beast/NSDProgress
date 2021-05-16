package net.corda.nrd.webserver

import net.corda.core.contracts.StateAndRef
import net.corda.core.internal.toX500Name
import net.corda.core.messaging.startFlow
import net.corda.core.node.NodeInfo
import net.corda.nrd.account.dto.AccountDTO
import net.corda.nrd.flows.*
import net.corda.nrd.flows.FiatCurrencyIssueFlow
import net.corda.nrd.flows.accountsUtilities.CreateNewAccount
import net.corda.nrd.flows.accountsUtilities.NewKeyForAccount
import net.corda.nrd.flows.accountsUtilities.ShareAccountTo
import net.corda.nrd.flows.accountsUtilities.ViewMyAccounts
import net.corda.nrd.states.StockState
import net.corda.nrd.token.rq.CreateTokenRq
import net.corda.nrd.token.rq.FiatCurrencyIssueRq
import net.corda.nrd.token.rq.MoveStockTokenRq
import net.corda.nrd.token.rq.RetrieveTokenRq
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.security.PublicKey
import java.util.*

@RestController
@RequestMapping("/")
class Controller(rpc: NodeRPCConnection) {

    private val proxy = rpc.proxy
    private val me = proxy.nodeInfo().legalIdentities.first().name
    private val log = LoggerFactory.getLogger(javaClass)

    fun X500Name.toDisplayString(): String = BCStyle.INSTANCE.toString(this)

    /** Helpers for filtering the network map cache. */
    private fun isNotary(nodeInfo: NodeInfo) = proxy.notaryIdentities().any { nodeInfo.isLegalIdentity(it) }
    private fun isMe(nodeInfo: NodeInfo) = nodeInfo.legalIdentities.first().name == me
    private fun isNetworkMap(nodeInfo: NodeInfo) =
        nodeInfo.legalIdentities.single().name.organisation == "Network Map Service"

    /**
     * Returns the node's name.
     */
    @GetMapping(value = ["me"], produces = [APPLICATION_JSON_VALUE])
    fun whoami() = mapOf("me" to me.toString())

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GetMapping(value = ["peers"], produces = [APPLICATION_JSON_VALUE])
    fun getPeers(): Map<String, List<String>> {
        return mapOf("peers" to proxy.networkMapSnapshot()
            .filter { isNotary(it).not() && isMe(it).not() && isNetworkMap(it).not() }
            .map { it.legalIdentities.first().name.toX500Name().toDisplayString() })
    }

    @PostMapping("/accounts/create")
    fun createAccount(@RequestBody(required = true) request: AccountDTO): ResponseEntity<Map<String, String>> {
        try {
            val result = proxy.startFlow(
                ::CreateNewAccount,
                request.individualTaxpayerNumber!!
            ).returnValue.get()
            return ResponseEntity.status(HttpStatus.CREATED).body(mapOf("user" to result)).also {
                log.debug("Account ${request.individualTaxpayerNumber} Created")
            }
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
    }

    // поделиться аккаунтами между узлами
    @PostMapping("/accounts/share/{whoAmI}/{shareTo}")
    fun shareAccount(@PathVariable whoAmI: String, @PathVariable shareTo: String): ResponseEntity<String> {
        try {
            val matchingPasties = proxy.partiesFromName(shareTo,false)
            val result = proxy.startFlow(
                ::ShareAccountTo,
                whoAmI,
                matchingPasties.first()
            ).returnValue.get()
            return ResponseEntity.status(HttpStatus.CREATED).body("Share Request has Sent")
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/accounts/my/accounts")
    fun viewMyAccounts(): ResponseEntity<List<String>> {
        try {
            val result = proxy.startFlow(::ViewMyAccounts).returnValue.get()
            return ResponseEntity.status(HttpStatus.CREATED).body(result)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/accounts/{accountId}/new-key")
    fun newKeyForAccount(@PathVariable accountId: UUID): ResponseEntity<PublicKey> {
        try {
            val result = proxy.startFlow(::NewKeyForAccount, accountId).returnValue.get()
            return ResponseEntity.status(HttpStatus.CREATED).body(result.owningKey)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
    }
}
