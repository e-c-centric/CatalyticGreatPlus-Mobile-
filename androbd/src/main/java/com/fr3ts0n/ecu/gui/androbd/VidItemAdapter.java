package com.fr3ts0n.ecu.gui.androbd;

import android.content.Context;

import com.fr3ts0n.pvs.PvList;

import java.util.Collection;

/**
 * Adapter to display OBD VID items from a process variable list
 *
 * 
 */
public class VidItemAdapter extends ObdItemAdapter
{
	public VidItemAdapter(Context context, int resource, PvList pvs)
	{
		super(context, resource, pvs);
	}

	@Override
	public Collection getPreferredItems(PvList pvs)
	{
		return pvs.values();
	}
}
