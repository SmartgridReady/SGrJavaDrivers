package de.re.easymodbus.adapter;

import com.smartgridready.driver.api.modbus.GenDriverAPI4Modbus;
import com.smartgridready.driver.api.common.GenDriverException;
import de.re.easymodbus.datatypes.DataBits;
import de.re.easymodbus.datatypes.Parity;
import de.re.easymodbus.datatypes.StopBits;
import de.re.easymodbus.modbusclient.ModbusClient;
import jssc.SerialPortException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenDriverAPI4ModbusRTUTest {
	private static final Logger LOG = LoggerFactory.getLogger(GenDriverAPI4ModbusRTUTest.class);
	
	private final int[] EXPECTED_RESPONSE = new int[] {0xAA, 2};
	
	@Mock
	ModbusClient modbusClient;
	
    @BeforeEach 
    public void initMocks() {
       MockitoAnnotations.openMocks(this);
    }
	
	@Test
	void readInputRegisters_success() throws Exception {
						
		
		GenDriverAPI4Modbus driver = new GenDriverAPI4ModbusRTU("COM9");
		setFieldByReflection(driver, "mbDevice", modbusClient);
		
		when(modbusClient.ReadHoldingRegisters(EXPECTED_RESPONSE[0], EXPECTED_RESPONSE[1])).thenReturn(EXPECTED_RESPONSE);
				
		int[] result = driver.ReadHoldingRegisters(EXPECTED_RESPONSE[0], EXPECTED_RESPONSE[1] );
		reportResult("Successful read register", result);
		assertArrayEquals(EXPECTED_RESPONSE, result);			
	}
	
	
	@SuppressWarnings("deprecation")
	@Test
	void readInputRegisters_throws_SerialPortException() throws Exception {
						
		
		GenDriverAPI4Modbus driver = new GenDriverAPI4ModbusRTU("COM9");
		setFieldByReflection(driver, "mbDevice", modbusClient);
		
		when(modbusClient.ReadHoldingRegisters(EXPECTED_RESPONSE[0], EXPECTED_RESPONSE[1])).thenThrow(new SerialPortException("COM9", "write", SerialPortException.TYPE_PORT_NOT_OPENED));
				
		GenDriverException e = assertThrows(GenDriverException.class, () ->
			driver.ReadHoldingRegisters(EXPECTED_RESPONSE[0], EXPECTED_RESPONSE[1]));
				
		assertEquals("Modbus read error: Port name - COM9; Method name - write; Exception type - Port not opened.", e.getMessage());		
	}
	

	@Test
	void initTrspServiceModbusRTU() throws Exception {
		
		// 1. overload
		GenDriverAPI4Modbus driver = new GenDriverAPI4ModbusRTU("COM1");
		setFieldByReflection(driver, "mbDevice", modbusClient);
		driver.connect();
		driver.disconnect();

		// TODO implement these tests in a different way

		/*
		ModbusClient modbusClient = (ModbusClient) getFieldByReflection(driver, "mbDevice");
		verify(modbusClient).setSerialPort("COM1");
		verify(modbusClient).setBaudrate(9200);
		verify(modbusClient).setParity(Parity.Even);
		verify(modbusClient).setDataBits(DataBits.Eight);
		verify(modbusClient).setStopBits(StopBits.One);
		Mockito.reset(modbusClient);
		*/

		// 2. overload
		driver = new GenDriverAPI4ModbusRTU("COM2", 19200);
		setFieldByReflection(driver, "mbDevice", modbusClient);
		driver.connect();
		driver.disconnect();

		/*
		verify(modbusClient).setSerialPort("COM2");
		verify(modbusClient).setBaudrate(19200);
		verify(modbusClient).setParity(Parity.Even);
		verify(modbusClient).setDataBits(DataBits.Eight);
		verify(modbusClient).setStopBits(StopBits.One);
		Mockito.reset(modbusClient);
		*/
		
		// 3. overload
		driver = new GenDriverAPI4ModbusRTU("COM1", 2400, com.smartgridready.driver.api.modbus.Parity.ODD);
		setFieldByReflection(driver, "mbDevice", modbusClient);
		driver.connect();
		driver.disconnect();

		/*
		verify(modbusClient).setSerialPort("COM1");
		verify(modbusClient).setBaudrate(2400);
		verify(modbusClient).setParity(Parity.Odd);
		verify(modbusClient).setDataBits(DataBits.Eight);
		verify(modbusClient).setStopBits(StopBits.One);
		Mockito.reset(modbusClient);
		*/
		
		// 4. overload
		driver = new GenDriverAPI4ModbusRTU("COM1", 2400,
				com.smartgridready.driver.api.modbus.Parity.ODD,
				com.smartgridready.driver.api.modbus.DataBits.SEVEN,
				com.smartgridready.driver.api.modbus.StopBits.ONE_AND_HALF);
		setFieldByReflection(driver, "mbDevice", modbusClient);
		driver.connect();
		driver.disconnect();

		/*
		verify(modbusClient).setSerialPort("COM1");
		verify(modbusClient).setBaudrate(2400);
		verify(modbusClient).setParity(Parity.Odd);
		verify(modbusClient).setDataBits(DataBits.Seven);
		verify(modbusClient).setStopBits(StopBits.OnePointFive);
		*/
	}
	
	private void reportResult(String testCase, int[] result) {
		StringBuffer sbuf = new StringBuffer();
		Arrays.stream(result).boxed().forEach( b -> sbuf.append(String.format("%x, ", b)));		
		LOG.info(testCase + " - result: {}", sbuf);
	}
			
	private void setFieldByReflection(Object object, String fieldName, Object value) 
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		Field f1 = getAccessibleField(object, fieldName);		
		f1.setAccessible(true);
		f1.set(object, value);
	}
	
	private Object getFieldByReflection(Object object, String fieldName) 
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		Field f1 = getAccessibleField(object, fieldName);
		return f1.get(object);
	}

	private Field getAccessibleField(Object object, String fieldName) throws NoSuchFieldException {		
		Optional<Field> field = Stream.concat(
			Arrays.asList(object.getClass().getDeclaredFields()).stream(),
			Arrays.asList(object.getClass().getSuperclass().getDeclaredFields()).stream()
		)
		.filter(f -> f.getName().equals(fieldName))
		.findFirst();
		
		if (field.isEmpty()) {
			throw new NoSuchFieldException(String.format("Field %s not found", fieldName));
		}

		field.get().setAccessible(true);
		return field.get();
	}
}
