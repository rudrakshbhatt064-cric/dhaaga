package com.dhaaga.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.format.DateUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val bg = Color.parseColor("#0E1523")
    private val surface = Color.parseColor("#172033")
    private val ink = Color.parseColor("#E8EDF6")
    private val muted = Color.parseColor("#9BA7BB")
    private val accent = Color.parseColor("#F0687E")
    private val onAccent = Color.parseColor("#2A0A13")

    private var inboxFrom: TextView? = null
    private var inboxText: TextView? = null

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        runOnUiThread { updateInbox() }
    }

    private val quick = listOf(
        "I miss you", "Love you", "Good morning", "Good night",
        "Call me", "Thinking of you", "Khana khaya?"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        if (Store.room(this).isNotEmpty()) MessageService.start(this)
        render()
    }

    override fun onResume() {
        super.onResume()
        Store.prefs(this).registerOnSharedPreferenceChangeListener(prefsListener)
        updateInbox()
    }

    override fun onPause() {
        Store.prefs(this).unregisterOnSharedPreferenceChangeListener(prefsListener)
        super.onPause()
    }

    // ---------- screens ----------

    private fun render() {
        inboxFrom = null
        inboxText = null
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(20), dp(28), dp(20), dp(40))
        scroll.addView(col)

        col.addView(label("Dhaaga", 30f, ink, true))
        if (Store.room(this).isEmpty()) buildJoin(col) else buildRoom(col)
        setContentView(scroll)
    }

    private fun buildJoin(col: LinearLayout) {
        col.addView(label("Dono phone mein ek hi room code daalna hai. Phir jo aap likhoge, woh unki home screen par aayega.", 15f, muted, false, 8))

        col.addView(label("Aapka naam", 13f, muted, false, 24))
        val nameEt = input("Jaise Aarav")
        nameEt.setText(Store.name(this))
        col.addView(nameEt)

        col.addView(button("Naya room banao", true) {
            val name = nameEt.text.toString().trim()
            if (name.isEmpty()) {
                toast("Pehle apna naam likhiye.")
            } else {
                Store.setName(this, name)
                Store.setRoom(this, Store.newRoomCode())
                MessageService.start(this)
                render()
            }
        }.also { margin(it, 16) })

        col.addView(label("ya", 14f, muted, false, 20).also { it.gravity = Gravity.CENTER })

        col.addView(label("Partner ka room code", 13f, muted, false, 12))
        val codeEt = input("Jaise k7m2xq9ptw")
        codeEt.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        col.addView(codeEt)

        col.addView(button("Room mein judo", false) {
            val name = nameEt.text.toString().trim()
            val code = codeEt.text.toString().trim().lowercase().replace(" ", "")
            if (name.isEmpty()) {
                toast("Pehle apna naam likhiye.")
            } else if (code.length < 6) {
                toast("Room code poora daaliye.")
            } else {
                Store.setName(this, name)
                Store.setRoom(this, code)
                MessageService.start(this)
                render()
            }
        }.also { margin(it, 16) })
    }

    private fun buildRoom(col: LinearLayout) {
        col.addView(label("Room code", 13f, muted, false, 20))
        val code = label(Store.room(this), 26f, ink, true, 2)
        code.setTextIsSelectable(true)
        col.addView(code)
        col.addView(button("Code partner ko bhejo", false) {
            val send = Intent(Intent.ACTION_SEND)
            send.type = "text/plain"
            send.putExtra(Intent.EXTRA_TEXT, "Dhaaga mein mere room mein aao. Room code: " + Store.room(this))
            startActivity(Intent.createChooser(send, "Code bhejo"))
        }.also { margin(it, 10) })

        col.addView(label("Unka sandesh", 13f, muted, false, 28))
        val from = label("", 13f, muted, false, 4)
        val text = label("", 30f, accent, true, 2)
        inboxFrom = from
        inboxText = text
        col.addView(from)
        col.addView(text)
        updateInbox()

        col.addView(label("Sandesh bhejo", 13f, muted, false, 28))
        val msg = input("Kuch likho")
        msg.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        msg.minLines = 2
        col.addView(msg)
        col.addView(button("Bhejo", true) {
            val t = msg.text.toString().trim()
            if (t.isNotEmpty()) {
                send(t) { ok -> if (ok) msg.setText("") }
            }
        }.also { margin(it, 10) })

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        for (q in quick) {
            val b = button(q, false) { send(q) { } }
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.rightMargin = dp(8)
            b.layoutParams = lp
            row.addView(b)
        }
        val hs = HorizontalScrollView(this)
        hs.isHorizontalScrollBarEnabled = false
        hs.addView(row)
        col.addView(hs)
        (hs.layoutParams as LinearLayout.LayoutParams).topMargin = dp(12)

        col.addView(label("Home screen par widget jodne ke liye khaali jagah par dabake rakhiye, phir Widgets mein Dhaaga chuniye.", 13f, muted, false, 28))

        col.addView(button("Room chhodo", false) {
            MessageService.stop(this)
            Store.clearAll(this)
            DhaagaWidget.refresh(this)
            render()
        }.also { margin(it, 20) })
    }

    // ---------- actions ----------

    private fun send(text: String, after: (Boolean) -> Unit) {
        Net.send(this, text) { ok ->
            runOnUiThread {
                if (ok) toast("Bhej diya: $text") else toast("Nahi gaya. Internet check karein.")
                after(ok)
            }
        }
    }

    private fun updateInbox() {
        val t = inboxText ?: return
        val f = inboxFrom ?: return
        val last = Store.lastText(this)
        if (last.isEmpty()) {
            f.text = ""
            t.text = "Abhi koi sandesh nahi"
            t.textSize = 18f
        } else {
            val ago = DateUtils.getRelativeTimeSpanString(
                Store.lastAt(this), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
            )
            f.text = Store.lastFrom(this) + " · " + ago
            t.text = last
            t.textSize = 30f
        }
    }

    // ---------- small view helpers ----------

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    private fun label(text: String, sp: Float, color: Int, bold: Boolean, topDp: Int = 0): TextView {
        val tv = TextView(this)
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
        tv.setTextColor(color)
        if (bold) tv.setTypeface(null, Typeface.BOLD)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(topDp)
        tv.layoutParams = lp
        return tv
    }

    private fun input(hint: String): EditText {
        val et = EditText(this)
        et.hint = hint
        et.setHintTextColor(muted)
        et.setTextColor(ink)
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        et.setPadding(dp(14), dp(12), dp(14), dp(12))
        val shape = GradientDrawable()
        shape.setColor(surface)
        shape.cornerRadius = dp(12).toFloat()
        et.background = shape
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(6)
        et.layoutParams = lp
        return et
    }

    private fun button(text: String, filled: Boolean, onClick: () -> Unit): Button {
        val b = Button(this)
        b.text = text
        b.isAllCaps = false
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        b.setTextColor(if (filled) onAccent else ink)
        val shape = GradientDrawable()
        shape.setColor(if (filled) accent else surface)
        shape.cornerRadius = dp(14).toFloat()
        b.background = shape
        b.setOnClickListener(View.OnClickListener { onClick() })
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        b.layoutParams = lp
        return b
    }

    private fun margin(v: View, topDp: Int) {
        (v.layoutParams as LinearLayout.LayoutParams).topMargin = dp(topDp)
    }
}
