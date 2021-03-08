package com.easv.aepm.listviewrecylerview.RecyclerAdapter

import android.content.Context
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.easv.aepm.listviewrecylerview.R
import com.easv.aepm.listviewrecylerview.data.BEFriend
import com.easv.aepm.listviewrecylerview.data.FriendRepository
import com.easv.aepm.listviewrecylerview.data.interfaces.IClickItemListener


class RecyclerAdapter: RecyclerView.Adapter<RecyclerHolder>{

    private var mInflater: LayoutInflater
    private var friendRepository: FriendRepository
    private var itemListener: IClickItemListener
    private var friendList: List<BEFriend>

    constructor(context: Context, friendRepository: FriendRepository, itemClickListener: IClickItemListener) : super(){
        this.mInflater = LayoutInflater.from(context)
        this.friendRepository = friendRepository
        this.itemListener = itemClickListener
        this.friendList = friendRepository.getAll()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder {
        val view: View = mInflater.inflate(R.layout.cell_extended, parent, false)
        return RecyclerHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
        val friend = friendList[position]
        holder.view.setOnClickListener { view -> itemListener.onItemClick(friend, position) }
        holder.view.setOnLongClickListener { view -> itemListener.onItemLongClick(friend, position, view); true }

        holder.bind(friend)
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    fun filter(text: String, favorite: Boolean) {

        var searchCopy: List<BEFriend> = friendRepository.getAll()

        if(text.isNotEmpty()){
            searchCopy = searchCopy.filter{ friend -> friend.name.toLowerCase().contains(text.toLowerCase())}
        }

        if(favorite){
            searchCopy = searchCopy.filter{ friend -> friend.isFavorite}
        }

        friendList = searchCopy
        notifyDataSetChanged()
    }
}


class RecyclerHolder(view: View) : RecyclerView.ViewHolder(view) {
    lateinit var friend: BEFriend
    val view: View = view
    val nameText: TextView = view.findViewById(R.id.name)
    val aliasText: TextView = view.findViewById(R.id.alias)
    val imageView: ImageView = view.findViewById(R.id.imgFavoriteExt)

    fun bind(friend: BEFriend){
        this.nameText.text = friend.name
        this.aliasText.text = "(${friend.alias})"
        this.friend = friend;
        this.imageView.setImageResource(if (friend.isFavorite) R.drawable.ok else R.drawable.notok)
    }

    init { }


}