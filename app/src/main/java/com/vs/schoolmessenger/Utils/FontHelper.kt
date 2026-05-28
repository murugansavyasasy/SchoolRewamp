import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.vs.schoolmessenger.R

object FontHelper {
    fun applyFont(view: View, context: Context) {
        val typeface = ResourcesCompat.getFont(context, R.font.poppins_regular)

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyFont(view.getChildAt(i), context)
            }
        } else if (view is TextView) {
            view.typeface = typeface
        }
    }
}
