package de.dhbw.heidenheim.wi2012.securechat;

import java.util.ArrayList;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
			holder.message = (TextView) convertView.findViewById(R.id.message_text);
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();
		
		holder.message.setText(message.getMessage());
		
		LayoutParams lp = (LayoutParams) holder.message.getLayoutParams();
		//check if it is a status message then remove background
		if(message.isStatusMessage())
		{
			holder.message.setBackground(null);
			lp.gravity = Gravity.LEFT;
		}
		else
		{		
			//Check whether message is mine to show green background and align to right
			if(message.isMine())
			{
				holder.message.setBackgroundResource(R.drawable.speech_bubble_me);
				lp.gravity = Gravity.RIGHT;
			}
			//If not mine then it is from sender to show orange background and align to left
			else
			{
				holder.message.setBackgroundResource(R.drawable.speech_bubble_opponent);
				lp.gravity = Gravity.LEFT;
			}
			holder.message.setLayoutParams(lp);
		}
		return convertView;
	}
	private static class ViewHolder
	{
		TextView message;
	}

	@Override
	public long getItemId(int position) {
		//Unimplemented, because we aren't using Sqlite.
		return position;
	}

}
