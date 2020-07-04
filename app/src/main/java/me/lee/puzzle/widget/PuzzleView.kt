package me.lee.puzzle.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import me.lee.puzzle.R
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.random.Random

/**
 *
 * @description    PuzzleView
 * @author         lihuayong
 * @date           2020-02-05 13:37
 * @version        1.0
 */
class PuzzleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val PIC = 1
        const val NAN = -1
        const val HOLO_BLUE_LIGHT = 0xff33b5e5.toInt()
//        const val HOLO_GREEN_LIGHT = 0xff99cc00.toInt()
    }

    /**
     * 拼图宽度
     */
    private var puzzleWidth = 0

    /**
     * 拼图块
     */
    private val cellList: ArrayList<Cell> = arrayListOf()

    private var isGameStart = false
    private var showFinal = false

    /**
     *
     *  -----------
     * | 0 | 1 | 2 |
     *  -----------
     * | 3 | 4 | 5 |
     *  -----------
     * | 6 | 7 | 8 |
     *  -----------
     * | - |
     *  ---
     */
    private var puzzleBitmap: Bitmap? = null

    private val emptyPaint: Paint = Paint().apply {
        color = Color.WHITE
    }
    private val linePaint: Paint = Paint().apply {
        strokeWidth = 10f
        color = HOLO_BLUE_LIGHT
    }
    private val textPaint: Paint = Paint().apply {
        strokeWidth = 30f
        color = Color.RED
        textSize = 100f
    }

    fun setBitmap(bitmap: Bitmap) {
        if (width == 0 || height == 0) {
            this.puzzleBitmap = bitmap
            return
        }
        this.puzzleBitmap = scaleBitmap(bitmap, puzzleWidth, puzzleWidth)
        isGameStart = false
        invalidate()
    }

    fun reStartGame() {
        isGameStart = true
        prepareCellList()
        invalidate()
    }

    fun showFinalBitmap() {
        if (!showFinal) {
            showFinal = true
            invalidate()
        }
    }

    fun hideFinalBitmap() {
        if (showFinal) {
            showFinal = false
            invalidate()
        }
    }

    init {
        puzzleBitmap = BitmapFactory.decodeResource(resources, R.mipmap.timg)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isGameStart || showFinal) {
            puzzleBitmap?.let {
                canvas.drawBitmap(it, 0f, 0f, null)
            }
        } else {
            puzzleWidth = if (width < height) width else height
            val cellSize = puzzleWidth / 3.toFloat()
            puzzleBitmap?.let {
                printCellList()

                for (cell in cellList) {
                    val left = cell.x * cellSize
                    val top = cell.y * cellSize
                    if (cell.status == NAN) {
//                        canvas.drawLine(left, top, left + cellSize, top + cellSize, textPaint)
                        canvas.drawRect(left, top + cellSize, left + cellSize, top, emptyPaint)
                    } else {
                        canvas.drawBitmap(
                            cell.bitmap!!,
                            left,
                            top,
                            null
                        )

                        val x = left + cellSize / 2 - 30
                        val y = top + cellSize / 2 + 30
                        canvas.drawText(cell.position.toString(), x, y, textPaint)
                    }
                }
            }

            //画方框
            drawFrame(canvas)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isGameStart) {
            dealTouch(event)
        }
        return super.onTouchEvent(event)
    }

    private fun dealTouch(event: MotionEvent) {
        val pointX = event.x
        val pointY = event.y
        val index = getTouchIndex(pointX.toInt(), pointY.toInt())

        if (index < 0 || index > 9) return
        Log.i("lhy", "touch index is $index")
        val neighbor = canMove(index)

        Log.i("lhy", "touch neighbor is $neighbor")

        if (neighbor != -1) {
            //swap
            val tx = cellList[index].x
            val ty = cellList[index].y
            cellList[index].x = cellList[neighbor].x
            cellList[index].y = cellList[neighbor].y
            cellList[neighbor].x = tx
            cellList[neighbor].y = ty
            sort()
            if (isGameStart && isOK()) {
                Toast.makeText(context, "恭喜你，完成啦！", Toast.LENGTH_LONG).show()
                isGameStart = false
                invalidate()
            }
            invalidate()
        }
    }

    private fun getTouchIndex(x: Int, y: Int): Int {
        return x * 3 / puzzleWidth + y * 3 / puzzleWidth * 3
    }

    private fun canMove(index: Int): Int {
        val cell = cellList[index]

        if (index == 6 && cellList[9].status == NAN) return 9
        if (index == 9) {
            return if (cellList[6].status == NAN) {
                6
            } else {
                NAN
            }
        }
        Log.i(
            "lhy",
            "can move, cell.x = ${cell.x}, cell.y = ${cell.y} cell.position = ${cell.position}"
        )
        if (cell.status == NAN) return -1

        val px = index % 3
        val py = index / 3
        //check left square
        if (px - 1 >= 0) {
            val preIndex = px + py * 3 - 1
            Log.i("lhy", "left index is $preIndex")
            if (cellList[preIndex].status == NAN)
                return preIndex
        }

        //check right square
        if (px + 1 < 3) {
            val afterIndex = px + py * 3 + 1
            Log.i("lhy", "right index is $afterIndex")

            if (cellList[afterIndex].status == NAN)
                return afterIndex
        }

        //check top square
        if (py - 1 >= 0) {
            val topIndex = (py - 1) * 3 + px
            Log.i("lhy", "top index is $topIndex")

            if (cellList[topIndex].status == NAN)
                return topIndex
        }

        //check bottom square
        if (py + 1 < 3) {
            val belowIndex = (py + 1) * 3 + px
            Log.i("lhy", "below index is $belowIndex")

            if (cellList[belowIndex].status == NAN)
                return belowIndex
        }
        return -1
    }

    private fun isOK(): Boolean {
        for (cell in cellList) {
            if (cell.status == NAN) continue
            if (cell.position != (cell.x + cell.y * 3))
                return false
        }
        return true
    }

    private fun drawFrame(canvas: Canvas) {
        val cellSize = puzzleWidth / 3.toFloat()
        for (i in 0..3) {
            //draw vertical line
            canvas.drawLine(
                i * cellSize,
                0f,
                i * cellSize,
                puzzleWidth.toFloat(),
                linePaint
            )

            //draw horizontal line
            canvas.drawLine(
                0f,
                i * cellSize,
                puzzleWidth.toFloat(),
                i * cellSize,
                linePaint
            )
        }

        canvas.drawLine(
            0f,
            puzzleWidth.toFloat(),
            0f,
            puzzleWidth.toFloat() + cellSize,
            linePaint
        )

        canvas.drawLine(
            cellSize,
            puzzleWidth.toFloat(),
            cellSize,
            puzzleWidth.toFloat() + cellSize,
            linePaint
        )

        canvas.drawLine(
            0f,
            puzzleWidth.toFloat() + cellSize,
            cellSize,
            puzzleWidth.toFloat() + cellSize,
            linePaint
        )
    }

    private fun generateRandomSeq(): IntArray {
        val result = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8)

        //随即俩俩交换位置9次
        for (i in 0..8) {
            val index1 = Random.nextInt(result.size)
            var index2 = Random.nextInt(result.size)
            while (index2 == index1) {
                index2 = Random.nextInt(result.size)
            }

            val t = result[index1]
            result[index1] = result[index2]
            result[index2] = t
        }

        //最后保证6在index=6的位置，再交换位置一次，保证总的交换次数是【偶数次】，从而保证拼图有解
        for (i in 0..8) {
            if (result[i] == 6) {
                if (i != 6) {
                    result[i] = result[6]
                    result[6] = 6
                } else {
                    val t = result[1]
                    result[1] = result[2]
                    result[2] = t
                }
                break
            }
        }

        return result
    }

    private fun prepareCellList() {
        cellList.clear()
        puzzleBitmap?.let {
            val size = it.width / 3
            for (i in 0..8) {
                val posX = i % 3
                val posY = i / 3
                cellList.add(
                    Cell(
                        PIC,
                        posX,
                        posY,
                        i,
                        cropBitmap(it, posX * size, posY * size, size, size)
                    )
                )
            }
        }

        //生成随机排列
        val seq = generateRandomSeq()
        for (i in 0..8) {
            cellList[i].x = seq[i] % 3
            cellList[i].y = seq[i] / 3
        }

        cellList.add(Cell(NAN, 9 % 3, 9 / 3, 9, null))

        val tx = cellList[6].x
        val ty = cellList[6].y
        cellList[6].x = cellList[9].x
        cellList[6].y = cellList[9].y
        cellList[9].x = tx
        cellList[9].y = ty
        sort()
    }

    private fun sort() {
        cellList.sortWith(Comparator { p0, p1 ->
            if (p0.y != p1.y) {
                p0.y - p1.y
            } else {
                p0.x - p1.x
            }
        })
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val w = right - left
        val h = bottom - top
        puzzleWidth = if (w < h) w else h
        this.puzzleBitmap = scaleBitmap(puzzleBitmap!!, puzzleWidth, puzzleWidth)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        puzzleWidth = if (w < h) w else h
        this.puzzleBitmap = scaleBitmap(puzzleBitmap!!, puzzleWidth, puzzleWidth)
    }

    private fun printCellList() {
        for (cell in cellList) {
            Log.i(
                "lhy",
                "status = ${cell.status}, x = ${cell.x}, y = ${cell.y} position = ${cell.position}"
            )
        }
    }

    data class Cell(
        var status: Int = PIC,
        var x: Int = 0,
        var y: Int = 0,
        val position: Int = 0,
        var bitmap: Bitmap?
    )
}