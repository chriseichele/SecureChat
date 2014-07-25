package de.dhbw.heidenheim.wi2012.securechat;

import java.util.ArrayList;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
/**
 * ChatListAdapter is a Custom class to implement custom row in ListView
 * 
 * @author 
 *
 */
public class ChatListAdapter extends BaseAdapter{
	private Context mContext;
	private ArrayList<Message> mMessages;



	public ChatListAdapter(Context context, ArrayList<Message> messages) {
		super();
		this.mContext = context;
		this.mMessages = messages;
	}
	@Override
	public int getCount() {
		return mMessages.size();
	}
	@Override
	public Object getItem(int position) {		
		return mMessages.get(position);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Message message = (Message) this.getItem(position);

		ViewHolder holder; 
		if(convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.message_row, parent, false);
			holder.message_bubble = (LinearLayout) convertView.findViewById(R.id.message_bubble);
			holder.message_text = (TextView) convertView.findViewById(R.id.message_text);
			holder.message_time = (TextView) convertView.findViewById(R.id.message_time);
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();

		holder.message_text.setText(message.getMessage());
		holder.message_time.setText(message.getMessageTime());
		
		LayoutParams lp = (LayoutParams) holder.message_bubble.getLayoutParams();
		if(message.isMine())
			{
				holder.message_bubble.setBackgroundResource(R.drawable.speech_bubble_me);
				lp.gravity = Gravity.RIGHT;
			}
			//If not mine then it is from sender to show orange background and align to left
			else
			{
				holder.message_bubble.setBackgroundResource(R.drawable.speech_bubble_opponent);
				lp.gravity = Gravity.LEFT;
			}
			holder.message_bubble.setLayoutParams(lp);
		return convertView;
	}
	private static class ViewHolder
	{
		LinearLayout message_bubble;
		TextView message_text;
		TextView message_time;
	}

	@Override
	public long getItemId(int position) {
		//Unimplemented, because we aren't using Sqlite.
		return position;
	}

}
