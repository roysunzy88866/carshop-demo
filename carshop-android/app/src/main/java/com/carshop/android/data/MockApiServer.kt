package com.carshop.android.data

import android.content.Context
import android.util.Log
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.io.IOException

// 用 OkHttp MockWebServer 起本地服务器,根据 request path 路由到 assets/mocks/*.json
// 在 CarshopApplication.onCreate 启动(USE_MOCK=true 时),baseUrl 喂给 ApiClient.init
//
// fixtures 来源:artifacts/fixtures/02~04 → assets/mocks/(07 session 重导过)
// 不允许自己捏字段(SPEC §15.2 + CLAUDE.md 共享约定 #10)
object MockApiServer {
    private const val TAG = "MockApiServer"
    private var server: MockWebServer? = null

    fun start(context: Context): String {
        // MockWebServer.start() bind ServerSocket + .url() 做 DNS canonical lookup,
        // 两件事都是网络操作,Android 不允许主线程 → 全部塞到后台 Thread 里 join 等
        val s = MockWebServer()
        s.dispatcher = AssetsDispatcher(context)
        var baseUrl = ""
        val t = Thread {
            s.start()  // 端口随机
            baseUrl = s.url("/api/v1/").toString()
        }
        t.start()
        t.join()
        server = s
        Log.i(TAG, "MockWebServer started at $baseUrl")
        return baseUrl
    }

    fun stop() {
        server?.shutdown()
        server = null
    }
}

private class AssetsDispatcher(private val context: Context) : Dispatcher() {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.path.orEmpty()
        val method = request.method.orEmpty()
        Log.d("MockApiServer", "→ $method $path")

        val (asset, status) = route(method, path)
        return if (asset == null) {
            MockResponse()
                .setResponseCode(404)
                .setBody("""{"code":1001,"data":null,"message":"mock: route not matched: $method $path"}""")
                .setHeader("Content-Type", "application/json")
        } else {
            val body = readAsset(asset)
            MockResponse()
                .setResponseCode(status)
                .setBody(body)
                .setHeader("Content-Type", "application/json")
        }
    }

    // 简单路由表 · 09 起按 query 参数细分:
    //   GET /products(无 category_id 或 page_size>=12)→ products.json 12 条全集(首页推荐)
    //   GET /products?category_id=*&page=N&page_size=10 → products-cat-pN.json(US-03 分页验证)
    //   GET /products/1 → product-1.json(详情 happy)
    //   GET /products/2 → product-2-offsale.json(US-02.2 下架商品)
    //   GET /products/13 → product-13-longtitle.json(US-24 1000 字符 title)
    //   GET /products/{其他} → product-404.json
    private fun route(method: String, path: String): Pair<String?, Int> {
        val clean = path.substringBefore('?')
        val query = parseQuery(path)
        return when {
            method == "GET" && clean == "/api/v1/categories" -> "categories.json" to 200
            method == "GET" && clean == "/api/v1/banners" -> "banners.json" to 200
            method == "GET" && clean == "/api/v1/products" -> routeProductsList(query)
            method == "GET" && clean.matches(Regex("/api/v1/products/\\d+")) -> {
                when (val id = clean.substringAfterLast('/')) {
                    "1" -> "product-1.json" to 200
                    "2" -> "product-2-offsale.json" to 200
                    "13" -> "product-13-longtitle.json" to 200
                    else -> "product-404.json".also { Log.d("MockApiServer", "product detail miss id=$id") } to 404
                }
            }
            method == "GET" && clean == "/api/v1/orders" -> routeOrderList(query)
            method == "GET" && clean.matches(Regex("/api/v1/orders/[^/]+")) -> "order-detail.json" to 200
            method == "POST" && clean == "/api/v1/orders" -> "order-create.json" to 200
            method == "POST" && clean.matches(Regex("/api/v1/orders/[^/]+/mock_pay")) -> "order-pay.json" to 200
            else -> null to 404
        }
    }

    // 10 session · 订单列表按 status 切换:
    //   no status / status=paid → orders.json(seed 里那条已支付订单)
    //   status=pending → orders-empty.json(mock 没造待支付订单,用空列表演示 Empty 态)
    // 真后端不走这里,联调用 carshop.hearagain.space 真数据。
    private fun routeOrderList(q: Map<String, String>): Pair<String?, Int> {
        return when (q["status"]) {
            "pending" -> "orders-empty.json" to 200
            else -> "orders.json" to 200
        }
    }

    private fun routeProductsList(q: Map<String, String>): Pair<String?, Int> {
        val pageSize = q["page_size"]?.toIntOrNull() ?: 20
        // 分类列表场景:UI 层走 page_size=10,触发分页 fixtures(total=25,3 页)
        if (pageSize == 10) {
            return when (q["page"]?.toIntOrNull() ?: 1) {
                1 -> "products-cat-p1.json" to 200
                2 -> "products-cat-p2.json" to 200
                3 -> "products-cat-p3.json" to 200
                else -> "products-cat-p3.json" to 200   // UI 靠 total=25 收手,不会越界
            }
        }
        // 默认:首页推荐 / page_size=20 → products.json (12 条全集)
        return "products.json" to 200
    }

    private fun parseQuery(path: String): Map<String, String> {
        val q = path.substringAfter('?', "")
        if (q.isEmpty()) return emptyMap()
        return q.split('&').mapNotNull { kv ->
            val parts = kv.split('=', limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()
    }

    private fun readAsset(name: String): String {
        return try {
            context.assets.open("mocks/$name").bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e("MockApiServer", "asset not found: mocks/$name", e)
            """{"code":9000,"data":null,"message":"mock asset missing: $name"}"""
        }
    }
}
