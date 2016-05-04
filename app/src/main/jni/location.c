//
// Created by lukas on 5/4/16.
//

/*
 * Generic method to call get_location_values methods in Java
 * for different providers
 *
 * Returns location values double array for called provider
 *
 */
double[] _get_location_values(const char *location_provider_method_name) {

}

/*
 * Calls get location for different location providers
 * (currently GPS and Network) and returns one dictionary
 * to Python
 */
PyObject* location_get_location() {
    double[] values_gps = _get_location_values("getLocationValuesGPS");
    double[] values_network = _get_location_values("getLocationValuesNetwork");

    int i = 0;
    for (i < )
}