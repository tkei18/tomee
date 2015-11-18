/**
 * Tomitribe Confidential
 * <p/>
 * Copyright(c) Tomitribe Corporation. 2014
 * <p/>
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 * <p/>
 */
package org.apache.openejb.resource.activemq;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import java.util.ArrayList;
import java.util.Iterator;

public class ConnectionWrapper implements Connection {

    private final ArrayList<SessionWrapper> sessions = new ArrayList<SessionWrapper>();

    private final Connection con;

    public ConnectionWrapper(final Connection con) {
        this.con = con;
    }

    @Override
    public Session createSession(final boolean transacted, final int acknowledgeMode) throws JMSException {
        return getSession(con.createSession(transacted, acknowledgeMode));
    }

    private Session getSession(final Session session) {
        final SessionWrapper wrapper = new SessionWrapper(this, session);
        sessions.add(wrapper);
        return wrapper;
    }

    protected void remove(final SessionWrapper wrapper) {
        sessions.remove(wrapper);
    }

    @Override
    public String getClientID() throws JMSException {
        return con.getClientID();
    }

    @Override
    public ConnectionConsumer createDurableConnectionConsumer(final Topic topic, final String subscriptionName, final String messageSelector, final ServerSessionPool sessionPool, final int maxMessages) throws JMSException {
        return con.createDurableConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool, maxMessages);
    }

    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        return con.getExceptionListener();
    }

    @Override
    public void setClientID(final String clientID) throws JMSException {
        con.setClientID(clientID);
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(final Destination destination, final String messageSelector, final ServerSessionPool sessionPool, final int maxMessages) throws JMSException {
        return con.createConnectionConsumer(destination, messageSelector, sessionPool, maxMessages);
    }

    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        return con.getMetaData();
    }

    @Override
    public void close() throws JMSException {

        final Iterator<SessionWrapper> iterator = sessions.iterator();

        while (iterator.hasNext()) {
            final SessionWrapper next = iterator.next();
            iterator.remove();
            try {
                next.close();
            } catch (final Exception e) {
                //no-op
            }
        }

        try {
            con.close();
        } finally {
            ConnectionFactoryWrapper.remove(this);
        }
    }

    @Override
    public void stop() throws JMSException {
        con.stop();
    }

    @Override
    public void setExceptionListener(final ExceptionListener listener) throws JMSException {
        con.setExceptionListener(listener);
    }

    @Override
    public void start() throws JMSException {
        con.start();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ConnectionWrapper that = (ConnectionWrapper) o;

        return con.equals(that.con);

    }

    @Override
    public int hashCode() {
        return con.hashCode();
    }
}
