package me.lee.puzzle

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_select_picture.view.*
import kotlinx.android.synthetic.main.item_view_pic.view.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val SHARE_PRE_NAME = "PuzzleGame"
        const val RESIST_PICTURE_ID = "picture_id"
    }

    private val pictures =
        arrayListOf(
            R.mipmap.bug,
            R.mipmap.bug2,
            R.mipmap.bug3,
            R.mipmap.timg,
            R.mipmap.konglong,
            R.mipmap.konglong2,
            R.mipmap.konglong3,
            R.mipmap.piggy,
            R.mipmap.xbx

        )
    private val names =
        arrayListOf("爆笑虫子", "爆笑虫子", "爆笑虫子", "加菲猫", "恐龙乐园", "小恐龙", "小恐龙", "小猪佩奇", "熊本熊")

    private lateinit var dialog: BottomSheetDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.requestFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)

        dialog = BottomSheetDialog(this)

        val pfsId =
            getSharedPreferences(SHARE_PRE_NAME, Context.MODE_PRIVATE).getInt(RESIST_PICTURE_ID, -1)

        val resId = if (pfsId == -1) R.mipmap.piggy else pfsId
        val bitmap = BitmapFactory.decodeResource(resources, resId)
        puzzleView.setBitmap(bitmap)

        btnReStart.setOnClickListener {
            puzzleView.reStartGame()
            btnReStart.text = "重新开始"
        }

        btnShow.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_BUTTON_PRESS ||
                event.action == MotionEvent.ACTION_MOVE
            ) {
                puzzleView.showFinalBitmap()
            } else {
                puzzleView.hideFinalBitmap()
            }
            return@setOnTouchListener false
        }

        btnChange.setOnClickListener {
            dialog.show()
        }

        initDialog()
    }

    private fun initDialog() {
        dialog.setContentView(R.layout.dialog_select_picture)
        dialog.window?.run {
            decorView.recyclerView.layoutManager = LinearLayoutManager(context)
            decorView.recyclerView.adapter = Adapter(names, pictures) { _, picId ->
                val bitmap = BitmapFactory.decodeResource(resources, picId)
                puzzleView.setBitmap(bitmap)

                btnReStart.text = "开始游戏"
                getSharedPreferences(SHARE_PRE_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putInt(RESIST_PICTURE_ID, picId)
                    .apply()
                dialog.dismiss()
            }
        }
    }

    class Adapter(
        private val name: List<String>,
        private val pic: List<Int>,
        private val block: (name: String, picId: Int) -> Unit
    ) :
        RecyclerView.Adapter<VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_view_pic,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int {
            return name.size
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.setData(name[position], pic[position])
            holder.itemView.setOnClickListener {
                block(name[position], pic[position])
            }
        }

    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        fun setData(name: String, resId: Int) {
            itemView.tvName.text = name
            itemView.ivPicture.setImageResource(resId)
        }
    }
}
