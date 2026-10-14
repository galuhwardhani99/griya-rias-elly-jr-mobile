package wardhani.galuh.project_griyarias.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import wardhani.galuh.project_griyarias.data.CalendarDay
import wardhani.galuh.project_griyarias.databinding.ItemCalendarDayBinding

class CalendarAdapter(
    private var days: List<CalendarDay>,
    private val onItemClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    inner class CalendarViewHolder(val binding: ItemCalendarDayBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CalendarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val item = days[position]
        holder.binding.tvDayNumber.text = item.dayOfMonth

        if (!item.isCurrentMonth) {
            // Tanggal luar bulan ini (transparan / tersembunyi)
            holder.binding.cardDay.setCardBackgroundColor(Color.TRANSPARENT)
            holder.binding.tvDayNumber.text = ""
            holder.binding.cardDay.setOnClickListener(null)
            return
        }

        // Warna Hijau untuk Kosong, Merah untuk Terisi
        if (item.isBooked) {
            holder.binding.cardDay.setCardBackgroundColor(Color.parseColor("#E57373")) // Merah
        } else {
            holder.binding.cardDay.setCardBackgroundColor(Color.parseColor("#81C784")) // Hijau
        }

        holder.binding.cardDay.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = days.size

    fun updateData(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }
}