package tn.esprit.signature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

class ResultFragment : Fragment() {

    companion object {
        fun newInstance(isVerified: Boolean): ResultFragment {
            return ResultFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isVerified", isVerified)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_result, container, false)
            .apply {
                val isVerified = this@ResultFragment.arguments!!.getBoolean("isVerified")
                findViewById<ImageView>(R.id.verified_image).visibility =
                    if (isVerified) View.VISIBLE else View.GONE
                findViewById<ImageView>(R.id.error_image).visibility =
                    if (isVerified) View.GONE else View.VISIBLE
            }
    }

}