package fr.wakemybus.playground.util;

import com.squareup.otto.Bus;

/**
 * Created by thibaultguegan on 17/02/15.
 */
public class BusProvider {

    private static final Bus UI_BUS = new Bus();

    private BusProvider() {};

    public static Bus getUIBusInstance () {

        return UI_BUS;
    }
}
