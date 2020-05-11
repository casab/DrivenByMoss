// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2020
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.launchpad.view;

import de.mossgrabers.framework.controller.grid.IVirtualFaderCallback;
import de.mossgrabers.framework.view.View;
import de.mossgrabers.framework.view.ViewManager;


/**
 * Links a virtual fader to a fader view.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class VirtualFaderViewCallback implements IVirtualFaderCallback
{
    private final int         index;
    private final ViewManager viewManager;


    /**
     * Constructor.
     *
     * @param index The index of the fader (0-7)
     * @param viewManager The viewManager
     */
    public VirtualFaderViewCallback (final int index, final ViewManager viewManager)
    {
        this.index = index;
        this.viewManager = viewManager;
    }


    /** {@inheritDoc} */
    @Override
    public int getValue ()
    {
        final View activeView = this.viewManager.getActiveView ();
        if (activeView instanceof AbstractFaderView)
            return ((AbstractFaderView) activeView).getFaderValue (this.index);
        return 0;
    }


    /** {@inheritDoc} */
    @Override
    public void setValue (final int value)
    {
        final View activeView = this.viewManager.getActiveView ();
        if (activeView instanceof AbstractFaderView)
            ((AbstractFaderView) activeView).onValueKnob (this.index, value);
    }
}
