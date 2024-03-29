package com.enaboapps.switchify.keyboard.prediction

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.enaboapps.switchify.keyboard.KeyType
import com.enaboapps.switchify.keyboard.KeyboardKey

class PredictionView : LinearLayout {

    private lateinit var onPredictionTapped: (KeyType.Prediction) -> Unit

    constructor(
        context: Context,
        onPredictionTapped: (KeyType.Prediction) -> Unit
    ) : super(context) {
        this.onPredictionTapped = onPredictionTapped
        orientation = HORIZONTAL
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        orientation = HORIZONTAL
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        orientation = HORIZONTAL
    }

    fun setPredictions(predictions: List<String>) {
        removeAllViews()
        for (prediction in predictions) {
            val predictionKey = KeyboardKey(context).apply {
                setKeyContent(prediction)
                action = { onPredictionTapped(KeyType.Prediction(prediction)) }
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
            }
            addView(predictionKey)
        }
    }
}