/**
 * Copyright (c) 2014, University of Warsaw
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package pl.edu.mimuw.cloudatlas.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * A zone management information object. This object is a single node in a zone hierarchy. It stores zone attributes as well as
 * references to its father and sons in the tree.
 */
public class ZMI implements Cloneable {
    public class NoSuchZoneException extends Exception {
        public NoSuchZoneException(PathName path) {
            super("No such zone: " + path);
        }
    }
    private final AttributesMap attributes = new AttributesMap();

    private final List<ZMI> sons = new ArrayList<ZMI>();
    private ZMI father;

    /**
     * Creates a new ZMI with no father (the root zone) and empty sons list.
     */
    public ZMI() {
        this(null);
    }

    /**
     * Creates a new ZMI with the specified node as a father and empty sons list. This method does not perform any
     * operation on <code>father</code>. In particular, setting this object <code>father</code>'s son must be done
     * separately.
     *
     * @param father the father of this ZMI
     * @see #addSon(ZMI)
     */
    public ZMI(ZMI father) {
        this.father = father;
    }

    /**
     * Gets the father of this ZMI.
     *
     * @return the father of this ZMI or <code>null</code> if this is the root zone
     */
    public ZMI getFather() {
        return father;
    }

    /**
     * Sets or changes the father of this ZMI in the tree. This method does not perform any operation on
     * <code>father</code>. In particular, setting this object as <code>father</code>'s son must be done separately.
     *
     * @param father a new father for this ZMI
     * @see #addSon(ZMI)
     */
    public void setFather(ZMI father) {
        this.father = father;
    }

    public ZMI findDescendant(PathName path) throws NoSuchZoneException {
        ZMI descendant = this;
        for (String component : path.getComponents()) {
            boolean foundNextSon = false;
            for (ZMI son : descendant.getSons()) {
                if (son.getAttributes().get("name").equals(new ValueString(component))) {
                    descendant = son;
                    foundNextSon = true;
                    break;
                }
            }

            if (!foundNextSon) {
                throw new NoSuchZoneException(path);
            }
        }

        return descendant;
    }

    /**
     * Gets the list of sons of this ZMI. Modifying a value in the returned list will cause an exception.
     *
     * @return the list of sons
     */
    public List<ZMI> getSons() {
        return Collections.unmodifiableList(sons);
    }

    /**
     * Adds the specified ZMI to the list of sons of this ZMI. This method does not perform any operation on
     * <code>son</code>. In particular, setting this object as <code>son</code>'s father must be done separately.
     *
     * @param son
     * @see #ZMI(ZMI)
     * @see #setFather(ZMI)
     */
    public void addSon(ZMI son) {
        sons.add(son);
    }

    /**
     * Removes the specified ZMI from the list of sons of this ZMI. This method does not perform any operation on
     * <code>son</code>. In particular, its father remains unchanged.
     *
     * @param son
     * @see #setFather(ZMI)
     */
    public void removeSon(ZMI son) {
        sons.remove(son);
    }

    /**
     * Gets a map of all the attributes stored in this ZMI.
     *
     * @return map of attributes
     */
    public AttributesMap getAttributes() {
        return attributes;
    }

    /**
     * Prints recursively in a prefix order (starting from this ZMI) a whole tree with all the attributes.
     *
     * @param stream a destination stream
     * @see #toString()
     */
    public void printAttributes(PrintStream stream) {
        for(Entry<Attribute, Value> entry : attributes)
            stream.println(entry.getKey() + " : " + entry.getValue().getType() + " = " + entry.getValue());
        System.out.println();
        for(ZMI son : sons)
            son.printAttributes(stream);
    }

    /**
     * Creates an independent copy of a whole hierarchy. A returned ZMI has the same reference as father (but the
     * father does not have a reference to it as a son). For the root zone, the copy is completely independent, since
     * its father is <code>null</code>.
     *
     * @return a deep copy of this ZMI
     */
    @Override
    public ZMI clone() {
        ZMI result = new ZMI(father);
        result.attributes.add(attributes.clone());
        for(ZMI son : sons) {
            ZMI sonClone = son.clone();
            result.sons.add(sonClone);
            sonClone.father = result;
        }
        return result;
    }

    /**
     * Prints a textual representation of this ZMI. It contains only attributes of this node.
     *
     * @return a textual representation of this object
     * @see #printAttributes(PrintStream)
     */
    @Override
    public String toString() {
        return attributes.toString();
    }

    /**
     * Gets the PathName representing this zone.
     *
     * @return a <code>PathName</code> object representing this zone
     */
    public PathName getPathName() {
        String name = ((ValueString)getAttributes().get("name")).getValue();
        return getFather() == null? PathName.ROOT : getFather().getPathName().levelDown(name);
    }

    public static ZMI deserialize(InputStream in) {
        Kryo kryo = new Kryo();
        Input kryoInput = new Input(in);
        ZMI zmi = kryo.readObject(kryoInput, ZMI.class);
        return zmi;
    }

    public void serialize(OutputStream out) {
        Kryo kryo = new Kryo();
        Output kryoOut = new Output(out);
        kryo.writeObject(kryoOut, this);
        kryoOut.flush();
    }
}
