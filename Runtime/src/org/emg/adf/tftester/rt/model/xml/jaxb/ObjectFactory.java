
package org.emg.adf.tftester.rt.model.xml.jaxb;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.emg.adf.tftester.rt.model.xml.jaxb package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.emg.adf.tftester.rt.model.xml.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TaskFlowTester }
     * 
     */
    public TaskFlowTester createTaskFlowTester() {
        return new TaskFlowTester();
    }

    /**
     * Create an instance of {@link ParamValueObject }
     * 
     */
    public ParamValueObject createParamValueObject() {
        return new ParamValueObject();
    }

    /**
     * Create an instance of {@link TaskFlow }
     * 
     */
    public TaskFlow createTaskFlow() {
        return new TaskFlow();
    }

    /**
     * Create an instance of {@link TestCase }
     * 
     */
    public TestCase createTestCase() {
        return new TestCase();
    }

}
