package com.example.carsellingandbuyingapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.bumptech.glide.Glide

class ItemAdapter(context: Context, private val items: List<Item>) :
    ArrayAdapter<Item>(context, R.layout.list_item_card_view, items) {

    private val shouldApplyAnimation = mutableSetOf<Int>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_card_view, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val item = items[position]
        Glide.with(context)
            .load(item.image2Url)
            .into(viewHolder.imageView)
        viewHolder.textView1.text = item.text1
        viewHolder.textView2.text = item.text2
        viewHolder.textView3.text = item.text3
        viewHolder.textView4.text = item.text4
        viewHolder.textView5.text = item.text5
        viewHolder.textView6.text = item.text6
        viewHolder.textView7.text = item.text7
        viewHolder.removeButton.visibility = View.INVISIBLE
        viewHolder.soldButton.visibility = View.INVISIBLE

        Glide.with(context)
            .load(item.image1Url)
            .into(viewHolder.imageView2)
        viewHolder.textView1.text = item.text1
        viewHolder.textView2.text = item.text2
        viewHolder.textView3.text = item.text3
        viewHolder.textView4.text = item.text4
        viewHolder.textView5.text = item.text5
        viewHolder.textView6.text = item.text6
        viewHolder.textView7.text = item.text7
        viewHolder.removeButton.visibility = View.INVISIBLE
        viewHolder.soldButton.visibility = View.INVISIBLE

        Glide.with(context)
            .load(item.image0Url)
            .into(viewHolder.imageView3)
        viewHolder.textView1.text = item.text1
        viewHolder.textView2.text = item.text2
        viewHolder.textView3.text = item.text3
        viewHolder.textView4.text = item.text4
        viewHolder.textView5.text = item.text5
        viewHolder.textView6.text = item.text6
        viewHolder.textView7.text = item.text7
        viewHolder.removeButton.visibility = View.INVISIBLE
        viewHolder.soldButton.visibility = View.INVISIBLE

        if (shouldApplyAnimation.contains(position)) {
            fadeIn(view)
            shouldApplyAnimation.remove(position)
        }

        return view
    }

    fun applyFadeInAnimation(position: Int) {
        shouldApplyAnimation.add(position)
    }

    private fun fadeIn(view: View) {
        val fadeInAnim = AlphaAnimation(0.0f, 1.0f)
        fadeInAnim.duration = 500
        view.startAnimation(fadeInAnim)
    }

    private class ViewHolder(view: View) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val imageView2: ImageView = view.findViewById(R.id.imageView2)
        val imageView3: ImageView = view.findViewById(R.id.imageView3)
        val textView1: TextView = view.findViewById(R.id.textView1)
        val textView2: TextView = view.findViewById(R.id.textView2)
        val textView3: TextView = view.findViewById(R.id.textView3)
        val textView4: TextView = view.findViewById(R.id.textView4)
        val textView5: TextView = view.findViewById(R.id.textView5)
        val textView6: TextView = view.findViewById(R.id.textView6)
        val textView7: TextView = view.findViewById(R.id.reg)
        val removeButton = view.findViewById<ImageView>(R.id.removeButton)
        val soldButton = view.findViewById<ImageView>(R.id.soldButton)
    }
}

