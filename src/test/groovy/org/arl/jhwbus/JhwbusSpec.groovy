package org.arl.jhwbus

import spock.lang.Specification
import org.arl.jhwbus.I2CDevice

class JhwbusSpec extends Specification{
    def "should be a simple assertion"() {
        expect:
        1==1
    }

    def "should be able to load I2CDevice"(){
        when :
        I2CDevice dev = I2CDevice.open("/dev/i2c-2", 0x76 as byte)

        then :
        dev != null
    }
}
