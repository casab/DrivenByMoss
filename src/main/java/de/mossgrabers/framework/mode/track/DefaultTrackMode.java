// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2021
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.mode.track;

import de.mossgrabers.framework.configuration.Configuration;
import de.mossgrabers.framework.controller.ContinuousID;
import de.mossgrabers.framework.controller.IControlSurface;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.featuregroup.AbstractMode;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;


/**
 * Base mode for track related modes.
 *
 * @param <S> The type of the control surface
 * @param <C> The type of the configuration
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class DefaultTrackMode<S extends IControlSurface<C>, C extends Configuration> extends AbstractMode<S, C, ITrack>
{
    /**
     * Constructor.
     *
     * @param name The name of the mode
     * @param surface The control surface
     * @param model The model
     * @param isAbsolute If true the value change is happening with a setter otherwise relative
     *            change method is used
     */
    public DefaultTrackMode (final String name, final S surface, final IModel model, final boolean isAbsolute)
    {
        this (name, surface, model, isAbsolute, null);
    }


    /**
     * Constructor.
     *
     * @param name The name of the mode
     * @param surface The control surface
     * @param model The model
     * @param isAbsolute If true the value change is happening with a setter otherwise relative
     *            change method is used
     * @param controls The IDs of the knobs or faders to control this mode
     */
    public DefaultTrackMode (final String name, final S surface, final IModel model, final boolean isAbsolute, final List<ContinuousID> controls)
    {
        this (name, surface, model, isAbsolute, controls, surface::isShiftPressed);
    }


    /**
     * Constructor.
     *
     * @param name The name of the mode
     * @param surface The control surface
     * @param model The model
     * @param isAbsolute If true the value change is happening with a setter otherwise relative
     *            change method is used
     * @param controls The IDs of the knobs or faders to control this mode
     * @param isAlternativeFunction Callback function to execute the secondary function, e.g. a
     *            shift button
     */
    public DefaultTrackMode (final String name, final S surface, final IModel model, final boolean isAbsolute, final List<ContinuousID> controls, final BooleanSupplier isAlternativeFunction)
    {
        super (name, surface, model, isAbsolute, model.getCurrentTrackBank (), controls, isAlternativeFunction);

        model.addTrackBankObserver (this::switchBanks);
    }


    /** {@inheritDoc} */
    @Override
    public Optional<String> getSelectedItemName ()
    {
        final Optional<ITrack> selectedItem = this.model.getCurrentTrackBank ().getSelectedItem ();
        if (selectedItem.isEmpty ())
            return Optional.empty ();
        final ITrack track = selectedItem.get ();
        return track.doesExist () ? Optional.of (track.getPosition () + 1 + ": " + track.getName ()) : Optional.empty ();
    }


    /**
     * Get the track for which to change the volume.
     *
     * @param index The index of the track. If set to -1 the selected track is used.
     * @return The selected track
     */
    protected Optional<ITrack> getTrack (final int index)
    {
        final ITrackBank tb = this.model.getCurrentTrackBank ();
        return index < 0 ? tb.getSelectedItem () : Optional.of (tb.getItem (index));
    }
}