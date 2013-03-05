package com.wardware.givingtracker.ui;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/*
 *	Use a formatter to format the cell Object
 */
public class FormatRenderer extends DefaultTableCellRenderer
{
    private static final SimpleDateFormat SDF = new SimpleDateFormat("MM/dd/yyyy");
	private Format formatter;

	/*
	 *   Use the specified formatter to format the Object
	 */
	public FormatRenderer(Format formatter)
	{
		this.formatter = formatter;
	}

	public void setValue(Object value)
	{
		//  Format the Object before setting its value in the renderer
		try
		{
			if (value != null) {
				value = formatter.format(value);
			}
		}
		catch(IllegalArgumentException e) {}

		super.setValue(value);
	}

	/*
	 *  Use the default date/time formatter for the default locale
	 */
	public static FormatRenderer getDateTimeRenderer()
	{
		return new FormatRenderer( DateFormat.getDateTimeInstance() );
	}

	/*
	 *  Use the default time formatter for the default locale
	 */
	public static FormatRenderer getTimeRenderer()
	{
		return new FormatRenderer( DateFormat.getTimeInstance() );
	}

    public static TableCellRenderer getSimpleDateRenderer()
    {
        return new FormatRenderer( SDF );
    }
}
