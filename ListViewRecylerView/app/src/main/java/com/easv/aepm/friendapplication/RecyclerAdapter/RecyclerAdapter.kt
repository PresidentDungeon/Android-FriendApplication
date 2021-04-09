package com.easv.aepm.friendapplication.RecyclerAdapter

import android.content.Context
import android.net.Uri
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.easv.aepm.friendapplication.DAL.FriendRepository
import com.easv.aepm.friendapplication.R
import com.easv.aepm.friendapplication.data.BEFriend
import com.easv.aepm.friendapplication.data.Sorting
import com.easv.aepm.friendapplication.data.interfaces.IClickItemListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File


class RecyclerAdapter: RecyclerView.Adapter<RecyclerHolder>{

    private var mInflater: LayoutInflater
    private var friendRepository: FriendRepository = FriendRepository.get()
    private var itemListener: IClickItemListener
    private var friendList: List<BEFriend> = emptyList()
    private var sortingType: Sorting = Sorting.SORTING_NAME
    private var context: Context

    constructor(context: Context, itemClickListener: IClickItemListener
    ) : super(){
        this.mInflater = LayoutInflater.from(context)
        this.itemListener = itemClickListener
        this.context = context

        val getDataJob = GlobalScope.async { friendRepository.getFriends("SELECT * FROM BEFriend ORDER BY name ASC", emptyArray()) }
        getDataJob.invokeOnCompletion { _ -> val myData = getDataJob.getCompleted(); this.friendList = myData; (context as AppCompatActivity).runOnUiThread { notifyDataSetChanged()}}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder {
        val view: View = mInflater.inflate(R.layout.cell_extended, parent, false)
        return RecyclerHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
        val friend = friendList[position]
        holder.view.setOnClickListener { view -> itemListener.onItemClick(friend, position) }
        holder.view.setOnLongClickListener { view -> itemListener.onItemLongClick(
            friend,
            position,
            view
        ); true }

        holder.bind(friend)
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    fun filter(text: String, favorite: Boolean) {

        var queryString: String = "SELECT * FROM BEFriend"
        var containsCondition: Boolean = false
        val args = mutableListOf<Any>()

        if(text.isNotEmpty()){
            queryString += " WHERE"
            queryString += " name LIKE '%' || ? || '%'"
            args.add(text)
            containsCondition = true
        }

        if(favorite){

            if(containsCondition){
                queryString += " AND"
            }else{
                queryString += " WHERE"
                containsCondition = true
            }

            queryString += " isFavorite = ?"
            args.add(favorite)
        }

        queryString += sortingType.query

        val getDataJob = GlobalScope.async {friendRepository.getFriends(queryString, args.toTypedArray()) }
        getDataJob.invokeOnCompletion { _ -> val myData = getDataJob.getCompleted(); this.friendList = myData
            (context as AppCompatActivity).runOnUiThread { notifyDataSetChanged()} }

    }

    fun setSortingType(sortingType: Sorting){
        this.sortingType = sortingType
    }

}


class RecyclerHolder(view: View) : RecyclerView.ViewHolder(view) {
    lateinit var friend: BEFriend
    val view: View = view
    val nameText: TextView = view.findViewById(R.id.name)
    val imageView: ImageView = view.findViewById(R.id.imgFavoriteExt)
    val imagePerson: ImageView = view.findViewById(R.id.imgProfile)

    fun bind(friend: BEFriend){
        this.nameText.text = friend.name
        this.friend = friend;
        this.imageView.setImageResource(if (friend.isFavorite) R.drawable.ok else R.drawable.notok)

        var file: File = File(this.friend.image)
        if (file!!.exists()){this.imagePerson.setImageURI(Uri.fromFile(file))}

    }

    init { }
}