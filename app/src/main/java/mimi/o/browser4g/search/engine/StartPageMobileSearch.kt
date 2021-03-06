package mimi.o.browser4g.search.engine

import mimi.o.browser4g.R
import android.app.Application

/**
 * The StartPage mobile search engine.
 */
class StartPageMobileSearch(application: Application?) : BaseSearchEngine(
    "file:///android_asset/startpage.png",
    "https://startpage.com/do/m/mobilesearch?language=english&query=",
    R.string.search_engine_startpage_mobile, application
)
