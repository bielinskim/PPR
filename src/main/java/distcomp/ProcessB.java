/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distcomp;

import java.util.Arrays;
import java.util.Random;
import javax.jms.*;

public class ProcessB extends Thread {

    private final Connection con;
    Session session;
    MessageConsumer consumerAB;
    MessageProducer producerBC;
    MessageProducer topicProducer;
    Random rand;
    int locaLogicalTime = 0;
    int remoteLogicalTime = 0;

    public ProcessB() throws JMSException {
        rand = new Random();
        ConnectionFactory factory = JmsProvider.getConnectionFactory();
        this.con = factory.createConnection();
        con.start();

        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue queueAB = session.createQueue("A-B");
        consumerAB = session.createConsumer(queueAB);
        Queue queueBC = session.createQueue("B-C");
        producerBC = session.createProducer(queueBC);

        Topic topic = session.createTopic("ReportTopic");
        topicProducer = session.createProducer(topic);
    }

    private void sleepRandomTime() {
        try {
            Thread.sleep((rand.nextInt(4) + 1) * 1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void sendMessage(String message) throws JMSException {
        TextMessage textMessage = session.createTextMessage(message);
        topicProducer.send(textMessage);
    }

    @Override
    public void run() {
        try {

            while (true) {
                ObjectMessage tm = (ObjectMessage) consumerAB.receive();
                if (tm == null) {
                    break;
                }
                double[] numbers = (double[]) tm.getObject();
                remoteLogicalTime = tm.getIntProperty("locaLogicalTime");
                if(remoteLogicalTime > locaLogicalTime) {
                    locaLogicalTime = remoteLogicalTime;
                }
                locaLogicalTime++;
                sendMessage("Proces B: odebrano dane z procesu A, czas: " + locaLogicalTime);
                sleepRandomTime();

                for (int i = 0; i < numbers.length; i++) {
                    numbers[i] = Math.log(numbers[i]);
                }
                locaLogicalTime++;
                sendMessage("Proces B: obliczono logarytm naturalny, czas: " + locaLogicalTime);
                sleepRandomTime();

                ObjectMessage objectMessage = session.createObjectMessage(numbers);
                objectMessage.setIntProperty("locaLogicalTime", locaLogicalTime);
                producerBC.send(objectMessage);
                locaLogicalTime++;
                sendMessage("Proces B: wysłano dane do procesu C, czas: " + locaLogicalTime);
                sleepRandomTime();

                locaLogicalTime++;
                sendMessage("Proces B: powrot do kroku 1, czas: " + locaLogicalTime);
                sleepRandomTime();

            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void destroy() throws JMSException {
        con.close();
    }
}
