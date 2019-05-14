package io.sulek.ssml

import android.annotation.SuppressLint
import android.util.AttributeSet
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children

class SimpleSwipeMenuLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, style: Int = 0) :
    ConstraintLayout(context, attrs, style) {

    private lateinit var backgroundContainer: View
    private lateinit var backgroundParams: LayoutParams
    private lateinit var foregroundContainer: View
    private lateinit var foregroundParams: LayoutParams

    private var onClickListener: OnClickListener? = null
    private var onSwipeListener: OnSwipeListener? = null

    private var positionOnActionDown = 0.0f
    private var distanceAfterMove = 0.0f
    private var calculatedNewMargin = 0
    private var backgroundContainerWidth = 0
    private var halfBackgroundContainerWidth = 0

    private var isExpanded = false
    private var isMenuOnTheLeft = true
    private var dynamicMenuWidth = true
    private var measureBackgroundContainerWidth = true

    init {
        attrs?.let { getAttributes(context, it) }
    }

    @SuppressLint("Recycle")
    private fun getAttributes(context: Context, attributeSet: AttributeSet) {
        context.obtainStyledAttributes(attributeSet, R.styleable.SimpleSwipeMenuLayout)?.let {
            isMenuOnTheLeft = false
            it.getInt(R.styleable.SimpleSwipeMenuLayout_menuSide, ATTR_LEFT_INT_VALUE) == ATTR_LEFT_INT_VALUE
            dynamicMenuWidth = true
            it.getBoolean(R.styleable.SimpleSwipeMenuLayout_dynamicMenuWidth, ATTR_DYNAMIC_MENU_WIDTH_DEFAULT)
            it.recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount != NUMBER_OF_REQUIRED_CHILDREN) throw Throwable(INCORRECT_NUMBER_OF_CHILDREN_MESSAGE)

        backgroundContainer = getChildAt(FOREGROUND_INDEX)
        foregroundContainer = getChildAt(BACKGROUND_INDEX)

        if (id == EMPTY_ID) throw Throwable(INCORRECT_MAIN_CONTAINER_ID_MESSAGE)
        if (backgroundContainer.id == EMPTY_ID) throw Throwable(INCORRECT_BACKGROUND_CONTAINER_ID_MESSAGE)
        if (foregroundContainer.id == EMPTY_ID) throw Throwable(INCORRECT_FOREGROUND_CONTAINER_ID_MESSAGE)

        setConstraints()

        foregroundParams = foregroundContainer.layoutParams as LayoutParams
        backgroundParams = backgroundContainer.layoutParams as LayoutParams

        if (backgroundParams.height != REQUIRED_BACKGROUND_CONTAINER_HEIGHT) throw Throwable(
            INCORRECT_BACKGROUND_CONTAINER_HEIGHT_MESSAGE
        )

        if (!dynamicMenuWidth) {
            backgroundContainerWidth = backgroundContainer.layoutParams.width
            halfBackgroundContainerWidth = backgroundContainerWidth / 2
        }

        enableBackgroundContainerClickEventsOnExpand()

        setOnTouchListener(getOnTouchListener())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (dynamicMenuWidth && measureBackgroundContainerWidth) {
            measureBackgroundContainerWidth = false
            measureBackgroundContainerWidth()
        }
    }

    private fun measureBackgroundContainerWidth() {
        backgroundContainerWidth = backgroundContainer.measuredWidth
        halfBackgroundContainerWidth = backgroundContainerWidth / 2
    }

    fun apply(isExpanded: Boolean) {
        this.isExpanded = isExpanded

        refreshForegroundContainerSwipeStatus()

        if (dynamicMenuWidth) {
            post {
                measureBackgroundContainerWidth()
                refreshForegroundContainerSwipeStatus()
            }
        }
    }

    private fun refreshForegroundContainerSwipeStatus() {
        if (isExpanded) expand() else collapse()
    }

    private fun enableBackgroundContainerClickEventsOnExpand() {
        (backgroundContainer as? ViewGroup)?.let { viewGroup ->
            viewGroup.children.forEach {
                it.isClickable = isExpanded
            }
        }
    }

    private fun setConstraints() {
        ConstraintSet().let {
            val sideConstraintSet = if (isMenuOnTheLeft) ConstraintSet.START else ConstraintSet.END

            it.clone(this)
            it.connect(backgroundContainer.id, sideConstraintSet, id, sideConstraintSet)
            it.connect(backgroundContainer.id, ConstraintSet.TOP, foregroundContainer.id, ConstraintSet.TOP)
            it.connect(backgroundContainer.id, ConstraintSet.BOTTOM, foregroundContainer.id, ConstraintSet.BOTTOM)
            it.applyTo(this)
        }
    }

    private fun getOnTouchListener() = OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> onPressDown(event.x)
            MotionEvent.ACTION_UP -> onReleasePress(false)
            MotionEvent.ACTION_CANCEL -> onReleasePress(true)
            MotionEvent.ACTION_MOVE -> onMove(event.x)
        }
        true
    }

    private fun onPressDown(position: Float) {
        positionOnActionDown = position
    }

    private fun onReleasePress(loseTouch: Boolean) {
        val wasSwiping = isSwiping

        if (calculatedNewMargin > halfBackgroundContainerWidth) expand()
        else collapse()

        onSwipeListener?.onSwipe(isExpanded)

        if (!loseTouch && !wasSwiping) onClickListener?.onClick(this)
    }

    private fun expand() {
        isExpanded = true
        calculatedNewMargin = backgroundContainerWidth
        refreshForegroundContainerMargin()
        clearDistanceAfterMove()
        enableBackgroundContainerClickEventsOnExpand()
    }

    private fun collapse() {
        isExpanded = false
        calculatedNewMargin = 0
        refreshForegroundContainerMargin()
        clearDistanceAfterMove()
        enableBackgroundContainerClickEventsOnExpand()
    }

    private fun clearDistanceAfterMove() {
        distanceAfterMove = 0f
    }

    private fun onMove(position: Float) {
        if (isExpanded) calculateMarginOnCollapsing(position) else calculateMarginOnExpanding(position)
        if (checkIfSwiping()) refreshForegroundContainerMargin()
    }

    private fun calculateMarginOnExpanding(position: Float) {
        distanceAfterMove = if (isMenuOnTheLeft) position - positionOnActionDown else positionOnActionDown - position
        calculatedNewMargin = when {
            distanceAfterMove < 0 -> 0
            distanceAfterMove > backgroundContainerWidth -> backgroundContainerWidth
            else -> distanceAfterMove.toInt()
        }
    }

    private fun calculateMarginOnCollapsing(position: Float) {
        distanceAfterMove = if (isMenuOnTheLeft) positionOnActionDown - position else position - positionOnActionDown
        calculatedNewMargin = when {
            distanceAfterMove < 0 -> backgroundContainerWidth
            distanceAfterMove > backgroundContainerWidth -> 0
            else -> (backgroundContainerWidth - distanceAfterMove).toInt()
        }
    }

    private fun refreshForegroundContainerMargin() {
        foregroundParams.leftMargin = calculatedNewMargin * (if (isMenuOnTheLeft) 1 else -1)
        foregroundParams.rightMargin = calculatedNewMargin * (if (isMenuOnTheLeft) -1 else 1)
        foregroundContainer.requestLayout()
    }

    private fun checkIfSwiping(): Boolean {
        isSwiping = distanceAfterMove > MIN_DISTANCE_TO_SWIPE
        return isSwiping
    }

    fun setOnSwipeListener(onSwipeListener: OnSwipeListener) {
        this.onSwipeListener = onSwipeListener
    }

    override fun setOnClickListener(onClickListener: OnClickListener?) {
        this.onClickListener = onClickListener
    }

    companion object {
        var isSwiping = false

        private const val ATTR_DYNAMIC_MENU_WIDTH_DEFAULT = true
        private const val ATTR_LEFT_INT_VALUE = 1
        private const val MIN_DISTANCE_TO_SWIPE = 10

        private const val NUMBER_OF_REQUIRED_CHILDREN = 2
        private const val FOREGROUND_INDEX = 0
        private const val BACKGROUND_INDEX = 1
        private const val INCORRECT_NUMBER_OF_CHILDREN_MESSAGE =
            "Incorrect number of children, required two: background container and foreground container"

        private const val EMPTY_ID = -1
        private const val INCORRECT_MAIN_CONTAINER_ID_MESSAGE =
            "Incorrect ID, main container (SimpleSwipeMenuLayout) should have ID to correct attach constraints"
        private const val INCORRECT_FOREGROUND_CONTAINER_ID_MESSAGE =
            "Incorrect ID, foreground container should have ID to correct attach constraints"
        private const val INCORRECT_BACKGROUND_CONTAINER_ID_MESSAGE =
            "Incorrect ID, background container should have ID to correct attach constraints"

        private const val REQUIRED_BACKGROUND_CONTAINER_HEIGHT = 0
        private const val INCORRECT_BACKGROUND_CONTAINER_HEIGHT_MESSAGE =
            "Incorrect height, background container should have 0dp height"
    }

}