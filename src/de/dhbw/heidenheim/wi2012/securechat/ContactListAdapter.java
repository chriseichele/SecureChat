package de.dhbw.heidenheim.wi2012.securechat;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
/**
 * ChatListAdapter is a Custom class to implement custom row in ListView
 * 
 * @author 
 *
 */
public class ContactListAdapter extends BaseAdapter{

	private Context mContext;
	private ArrayList<Contact> mContacts;

	public ContactListAdapter(Context context, ArrayList<Contact> contacts) {
		super();
		this.mContext = context;
		this.mContacts = contacts;
	}
	@Override
	public int getCount() {
		return mContacts.size();
	}
	@Override
	public Object getItem(int position) {		
		return mContacts.get(position);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Contact contact = (Contact) this.getItem(position);

		ViewHolder holder; 
		if(convertView == null) 
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.contact_row, parent, false);
			holder.contact_name = (TextView) convertView.findViewById(R.id.contact_name);
			holder.contact_id = (TextView) convertView.findViewById(R.id.contact_id);
			convertView.setTag(holder);
		} 
		else 
		{
			holder = (ViewHolder) convertView.getTag();
		}

		holder.contact_name.setText(contact.getName());
		holder.contact_id.setText(contact.getID());

		return convertView;
	}
	private static class ViewHolder
	{
		TextView contact_name;
		TextView contact_id;
	}

	@Override
	public long getItemId(int position) {
		//Unimplemented, because we aren't using Site.
		return position;
	}

}
