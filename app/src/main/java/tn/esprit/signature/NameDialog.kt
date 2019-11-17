package tn.esprit.signature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import java.util.*

interface DialogCallback {
    fun callback(name: String)
}

class NameDialog : DialogFragment() {

    companion object {
        fun newInstance(callback: DialogCallback): NameDialog {
            return NameDialog()
                .apply {
                    this.callbak = callback
                }
        }
    }

    var callbak: DialogCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inflatedView = inflater.inflate(R.layout.name_dialog, container, false)
        val nameEt: EditText = inflatedView.findViewById(R.id.name_input)
        val confirmButton: Button = inflatedView.findViewById(R.id.confirm_dialog_button)

        confirmButton.setOnClickListener {
            val name = nameEt.text.toString()
            if (name.isEmpty()) Toast.makeText(
                context,
                "Please write name",
                Toast.LENGTH_LONG
            ).show()
            else {
                callbak!!.callback(name.replace(" ", "_").toLowerCase(Locale.US))
            }
        }

        return inflatedView
    }

}