package mimi.o.browser4g

import mimi.o.browser4g.browser.activity.BrowserActivity
import mimi.o.browser4g.database.FeedsModel
import mimi.o.browser4g.database.feeds.FeedsDatabase
import mimi.o.browser4g.html.homepage.RssService
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import io.reactivex.Completable
import me.toptas.rssconverter.RssConverterFactory
import me.toptas.rssconverter.RssFeed
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.Exception

class MainActivity : BrowserActivity() {

    @Suppress("DEPRECATION")
    public override fun updateCookiePreference(): Completable = Completable.fromAction {
        val cookieManager = CookieManager.getInstance()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this@MainActivity)
        }
        cookieManager.setAcceptCookie(userPreferences.cookiesEnabled)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Handler().postDelayed({ getFeed() }, 1000)
    }

    fun getFeed(){
        val retrofit = Retrofit.Builder()
                .baseUrl("https://www.detik.com")
                .addConverterFactory(RssConverterFactory.create())
                .build()

        val service = retrofit.create(RssService::class.java)
        service.getRss("/feed/")
                .enqueue(object : Callback<RssFeed> {
                    override fun onResponse(call: Call<RssFeed>, response: Response<RssFeed>) {
                        if (!response.body()?.items.isNullOrEmpty()) {
                            val feedDb = FeedsDatabase(application)
                            feedDb.clearFeeds()

                            try {
                                for (item in response.body()?.items!!) {
                                    feedDb.feedEntry(FeedsModel(item.link!!, item.title!!, "https://www.detik.com", item.description!!))
                                }
                                Log.d("FEEDD", tabsManager.currentTab?.isNewTab.toString())
                                Log.d("FEEDD", tabsManager.currentTab?.title)
                                Log.d("FEEDD", tabsManager.currentTab?.url)

                                if(tabsManager.currentTab?.title.equals("Homepage5G", true)) {
                                    tabsManager.currentTab?.loadHomePage()
                                }
                            }catch (e: Exception){
                                Log.d("FEED", e.toString())
                            }
                        }
                    }

                    override fun onFailure(call: Call<RssFeed>, t: Throwable) {
                        Log.d("FEED", "fail")
                    }
                })
    }

    override fun onNewIntent(intent: Intent) =
        if (intent.action == INTENT_PANIC_TRIGGER) {
            panicClean()
        } else {
            handleNewIntent(intent)
            super.onNewIntent(intent)
        }

    override fun onPause() {
        super.onPause()
        saveOpenTabs()
    }

    override fun updateHistory(title: String?, url: String) = addItemToHistory(title, url)

    override fun isIncognito() = false

    override fun closeActivity() = closeDrawers {
        performExitCleanUp()
        moveTaskToBack(true)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.isCtrlPressed) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_P ->
                    // Open a new private window
                    if (event.isShiftPressed) {
                        startActivity(IncognitoActivity.createIntent(this))
                        overridePendingTransition(R.anim.slide_up_in, R.anim.fade_out_scale)
                        return true
                    }
            }
        }
        return super.dispatchKeyEvent(event)
    }


}
