package com.vs.schoolmessenger.CommonScreens.Ads

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.vs.schoolmessenger.Parent.Attendance.Attendance
import com.vs.schoolmessenger.Parent.Homework.HomeWork
import com.vs.schoolmessenger.Parent.Timetable.TimeTable
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.RestClient
import com.vs.schoolmessenger.School.AbsenteesMarking.AttendanceMark
import com.vs.schoolmessenger.School.Assignment.AssignmentReport
import com.vs.schoolmessenger.School.Attachment.AttachmentReport
import com.vs.schoolmessenger.School.DailyCollection.DailyCollection
import com.vs.schoolmessenger.School.Event.EventReport
import com.vs.schoolmessenger.School.ExamMarkUpload.ClassList.ClassList
import com.vs.schoolmessenger.School.FeePendingReport.FeePendingReport
import com.vs.schoolmessenger.School.Homework.HomeworkReport
import com.vs.schoolmessenger.School.LeaveRequests.LeaveRequests
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummary.LessonPlan
import com.vs.schoolmessenger.School.MarkYourAttendance.MarkYourAttendance
import com.vs.schoolmessenger.School.NoticeBoard.NoticeBoardReport
import com.vs.schoolmessenger.School.PTM.Activity.PTM
import com.vs.schoolmessenger.School.QuizExam.ExamQuiz
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ONE place for the banner ads shown inside the menu screens.
 *
 * School (sender) side: only the History list pages in [schoolAdScreens] show the ad,
 * each with its own menu id.
 *
 * Parent side - how it works
 *  1. ParentHomeFragment opens a menu with [putMenuId] -> the menu id goes in the Intent.
 *  2. BaseActivity calls [wrapContent]. When the Intent has a menu id, an ad slider (same
 *     AdSlider as the dashboard) is added right below the screen's toolbar (R.id.toolbarLayout).
 *     (or below the view set for that screen in adBelowViewId).
 *  3. The ads API is called with that menu id. status true -> ads shown, otherwise hidden.
 *
 * Screens that are not opened with a menu id (school side, notifications, ...) are untouched.
 *
 * To turn the menu ads off everywhere set [ENABLED] = false. Nothing else needs to change.
 */
object MenuAds {

    /** Master switch for the ads inside the menu screens. */
    const val ENABLED = true

    /** Menu id sent to the ads API for the dashboards (school and parent home). */
    const val DASHBOARD_MENU_ID = "0"

    private const val EXTRA_MENU_ID = "extra_ad_menu_id"
    private const val TAG = "MenuAds"

    /**
     * By default the ad goes right below R.id.toolbarLayout.
     * Screens where that breaks the design can place it below another view instead:
     * screen -> id of the view the ad should sit under.
     */
    private val adBelowViewId: Map<Class<out Activity>, Int> = mapOf(
        Attendance::class.java to R.id.cardCalendarDetails,
        HomeWork::class.java to R.id.cltDateSelection,
        // PTM: toolbarLayout wraps the whole screen, so the ad goes under the
        // Meetings / Booked Slots tabs; the lists and no-data image are layout_below lnrTabsContainer
        PTM::class.java to R.id.lnrTabsContainer
    )

    /** Screens that never show the menu ad (the ads API is not called for them either). */
    private val noAdScreens: Set<Class<out Activity>> = setOf(
        TimeTable::class.java
    )

    /**
     * School (sender) side: screens that show the ad -> menu id sent to the ads API.
     * For menus with a creation page (File, NoticeBoard, Homework, Task / Project) only the
     * History list page is listed, so the creation page never shows the ad.
     */
    private val schoolAdScreens: Map<Class<out Activity>, Int> = mapOf(
        AttachmentReport::class.java to Constant.M_ATTACHMENTS,                // File
        NoticeBoardReport::class.java to Constant.M_NOTICEBOARD,               // NoticeBoard
        HomeworkReport::class.java to Constant.M_HOMEWORK,                     // Homework
        AssignmentReport::class.java to Constant.M_ASSIGNMENT,                 // Task / Project
        EventReport::class.java to Constant.M_SCHOOL_CLASS_EVENTS,             // Events (History)
        AttendanceMark::class.java to Constant.M_ATTENDANCE_MARKING,           // Mark attendance
        LessonPlan::class.java to Constant.M_LESSON_PLAN,                      // Lesson plan
        ExamQuiz::class.java to Constant.M_QUIZ_EXAM,                          // Quiz
        PTM::class.java to Constant.M_PTM,                                     // PTM meeting
        DailyCollection::class.java to Constant.M_DAILY_COLLECTION,            // Daily collection
        FeePendingReport::class.java to Constant.M_FEE_PENDING_REPORT,         // Pending fee report
        MarkYourAttendance::class.java to Constant.M_MARK_YOUR_ATTENDANCE,     // Punch attendance (geo)
        LeaveRequests::class.java to Constant.M_LEAVE_REQUEST,                 // Approve student leave
        ClassList::class.java to Constant.M_UPLOAD_MARKS                       // Upload exam marks
    )

    /** Same token rule the school screens use: staff login, else the first school. */
    private fun schoolToken(activity: Activity): String? {
        val user = SharedPreference.getUserDetails(activity)
        return if (user?.staff_role == Constant.isStaffRole) {
            SharedPreference.getStaffDetails(activity)?.access_token
        } else {
            user?.staff_details?.firstOrNull()?.access_token
        }
    }

    /** Put the menu id on the Intent that opens a menu screen. */
    fun putMenuId(intent: Intent, menuId: Int): Intent =
        intent.putExtra(EXTRA_MENU_ID, menuId.toString())

    /**
     * Called by BaseActivity before setContentView. Returns the view to pass to setContentView.
     *
     * Works for any parent layout type (RelativeLayout, ConstraintLayout, LinearLayout...):
     * the toolbar is moved into a vertical container [toolbar + ad] that takes the toolbar's
     * place, layout params and id, so every "below toolbarLayout" rule / constraint in the
     * screen now sits below the ad. Screens without a toolbarLayout get no ad.
     */
    fun wrapContent(activity: Activity, content: View): View {
        if (!ENABLED || activity.javaClass in noAdScreens) return content

        // school (sender) side: only the History list pages, menu id from schoolAdScreens
        // parent side: any screen opened from the parent dashboard with a menu id
        val schoolMenuId = schoolAdScreens[activity.javaClass]
        val menuId: String
        val token: String?
        if (schoolMenuId != null) {
            menuId = schoolMenuId.toString()
            token = schoolToken(activity)
        } else {
            menuId = activity.intent?.getStringExtra(EXTRA_MENU_ID) ?: return content
            token = SharedPreference.getChildDetails(activity)?.access_token
        }
        if (token.isNullOrEmpty()) return content

        val anchorId = adBelowViewId[activity.javaClass] ?: R.id.toolbarLayout
        attachBelow(activity, content, anchorId, menuId, token, activity as? LifecycleOwner)
        return content
    }

    /**
     * Ads inside a FRAGMENT of a parent menu screen (call from onCreateView, after inflate).
     * Use it for menu screens whose toolbar lives in a fragment (e.g. Rewards / coupon Home).
     * Uses the menu id the hosting activity was opened with; nothing happens without one.
     *
     * @param anchorId the view the ad should sit under (the fragment's top bar)
     */
    fun attachToFragment(fragment: Fragment, root: View, anchorId: Int) {
        if (!ENABLED) return
        val activity = fragment.activity ?: return
        if (activity.javaClass in noAdScreens) return
        val menuId = activity.intent?.getStringExtra(EXTRA_MENU_ID) ?: return
        val token = SharedPreference.getChildDetails(activity)?.access_token
        if (token.isNullOrEmpty()) return
        attachBelow(activity, root, anchorId, menuId, token, fragment.viewLifecycleOwner)
    }

    /** Places the ad slider right below [anchorId] inside [root] and loads the ads. */
    private fun attachBelow(
        activity: Activity,
        root: View,
        anchorId: Int,
        menuId: String,
        token: String,
        lifecycleOwner: LifecycleOwner?
    ) {
        val anchor = root.findViewById<View>(anchorId)
        val parent = anchor?.parent as? ViewGroup
        if (anchor == null || parent == null) {
            Log.d(TAG, "${activity.javaClass.simpleName}: anchor view not found, ad skipped")
            return
        }

        val adView = LayoutInflater.from(activity)
            .inflate(R.layout.layout_menu_ad_slider, parent, false)

        // swap the anchor for an [anchor + ad] container at the same position
        val index = parent.indexOfChild(anchor)
        val params = anchor.layoutParams
        val anchorHeight =
            if (params.height > 0) params.height else ViewGroup.LayoutParams.WRAP_CONTENT
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        // side margins stay on the anchor (so the ad keeps the full width);
        // top / bottom margins (e.g. a negative overlap) stay on the container
        val innerParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, anchorHeight)
        if (params is ViewGroup.MarginLayoutParams) {
            innerParams.marginStart = params.marginStart
            innerParams.marginEnd = params.marginEnd
            params.marginStart = 0
            params.marginEnd = 0
            params.leftMargin = 0
            params.rightMargin = 0
        }
        parent.removeViewAt(index)

        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            id = anchorId                    // keeps every rule that points at the anchor
            clipChildren = false             // don't cut card shadows
            clipToPadding = false
        }
        anchor.id = View.generateViewId()    // binding already holds the anchor views
        container.addView(anchor, innerParams)
        container.addView(adView)
        parent.addView(container, index, params)

        val slider = AdSlider(
            adView,
            adView.findViewById(R.id.adViewPager),
            adView.findViewById(R.id.adDotsIndicator),
            adView.findViewById(R.id.imgCloseAd)
        )

        lifecycleOwner?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) = slider.start()
            override fun onPause(owner: LifecycleOwner) = slider.stop()
            override fun onDestroy(owner: LifecycleOwner) = slider.stop()
        })

        loadAds(activity, token, menuId, slider)
    }

    private fun loadAds(activity: Activity, token: String, menuId: String, slider: AdSlider) {
        val reportingUrl = SharedPreference.getReportingUrl(activity)
        if (reportingUrl.isNullOrEmpty()) {
            slider.hide()
            return
        }
        RestClient.changeApiBaseUrl(reportingUrl)
        RestClient.apiInterfaces.isGetAds(token, menuId)?.enqueue(object : Callback<AdsResponse?> {
            override fun onResponse(call: Call<AdsResponse?>, response: Response<AdsResponse?>) {
                if (activity.isFinishing || activity.isDestroyed) return
                Log.d(TAG, "menu $menuId -> ${response.code()}")
                slider.bind(if (response.isSuccessful) response.body() else null)
            }

            override fun onFailure(call: Call<AdsResponse?>, t: Throwable) {
                if (activity.isFinishing || activity.isDestroyed) return
                Log.e(TAG, "menu $menuId failed", t)
                slider.hide()
            }
        })
    }
}
