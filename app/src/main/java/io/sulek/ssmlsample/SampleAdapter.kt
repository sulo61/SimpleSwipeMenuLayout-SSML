package io.sulek.ssmlsample

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.sulek.ssml.OnSwipeListener
import kotlinx.android.synthetic.main.sample_holder.view.*
import java.util.*

class SampleAdapter(context: Context) : RecyclerView.Adapter<SampleAdapter.SampleHolder>() {

    private val inflater = LayoutInflater.from(context)
    private val items = mutableListOf<SampleData>()
    private val random = Random()

    init {
        prepareSampleData()
    }

    private fun prepareSampleData() {
        for (itemNumber in 1..30) {
            val randomNumberOfDescriptions = random.nextInt(10)
            var description = ""

            for (d in 1..randomNumberOfDescriptions) description += "description [$d]\n"

            items.add(
                SampleData(
                    "Item [$itemNumber] should have [$randomNumberOfDescriptions] lines of description",
                    description
                )
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleHolder {
        return SampleHolder(inflater.inflate(R.layout.sample_holder, parent, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: SampleHolder, position: Int) {
        holder.setSampleData(items[position])
    }

    inner class SampleHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setSampleData(sampleData: SampleData) {
            itemView.swipeContainer.setExpanded(sampleData.isExpanded)
            itemView.swipeContainer.setOnSwipeListener(object : OnSwipeListener {
                override fun onSwipe(isExpanded: Boolean) {
                    sampleData.isExpanded = isExpanded
                }
            })
            itemView.titleText.text = sampleData.title
            itemView.descriptionText.text = sampleData.description
        }
    }
}