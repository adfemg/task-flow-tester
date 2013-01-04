
package org.emg.adf.tftester.rt.model.xml.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://adf.emg.org/tftester}ParamValueObject" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="runInRegion" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="runAscall" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="stretchLayout" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "paramValueObject"
})
@XmlRootElement(name = "TestCase")
public class TestCase {

    @XmlElement(name = "ParamValueObject")
    protected List<ParamValueObject> paramValueObject;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute
    protected String description;
    @XmlAttribute
    protected Boolean runInRegion;
    @XmlAttribute
    protected Boolean runAscall;
    @XmlAttribute
    protected Boolean stretchLayout;

    /**
     * Gets the value of the paramValueObject property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the paramValueObject property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParamValueObject().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ParamValueObject }
     * 
     * 
     */
    public List<ParamValueObject> getParamValueObject() {
        if (paramValueObject == null) {
            paramValueObject = new ArrayList<ParamValueObject>();
        }
        return this.paramValueObject;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the runInRegion property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRunInRegion() {
        if (runInRegion == null) {
            return true;
        } else {
            return runInRegion;
        }
    }

    /**
     * Sets the value of the runInRegion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRunInRegion(Boolean value) {
        this.runInRegion = value;
    }

    /**
     * Gets the value of the runAscall property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRunAscall() {
        if (runAscall == null) {
            return false;
        } else {
            return runAscall;
        }
    }

    /**
     * Sets the value of the runAscall property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRunAscall(Boolean value) {
        this.runAscall = value;
    }

    /**
     * Gets the value of the stretchLayout property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isStretchLayout() {
        if (stretchLayout == null) {
            return true;
        } else {
            return stretchLayout;
        }
    }

    /**
     * Sets the value of the stretchLayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStretchLayout(Boolean value) {
        this.stretchLayout = value;
    }

}
