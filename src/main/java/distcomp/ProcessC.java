/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distcomp;

import java.util.Arrays;
import java.util.Random;
import javax.jms.*;

public class ProcessC extends Thread {

    private final Connection con;
    Session session;
    MessageConsumer consumerBC;
    MessageConsumer consumerDC;
    MessageProducer producerCA;
    MessageProducer topicProducer;
    Random rand;
    int locaLogicalTime = 0;
    int remoteLogicalTime = 0;

    public ProcessC() throws JMSException {
        rand = new Random();
        ConnectionFactory factory = JmsProvider.getConnectionFactory();
        this.con = factory.createConnection();
        con.start();

        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue queueBC = session.createQueue("B-C");
        consumerBC = session.createConsumer(queueBC);
        Queue queueDC = session.createQueue("D-C");
        consumerDC = session.createConsumer(queueDC);
        Queue queueCA = session.createQueue("C-A");
        producerCA = session.createProducer(queueCA);

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
                ObjectMessage tm = (ObjectMessage) consumerBC.receive();
                if (tm == null) {
                    break;
                }
                remoteLogicalTime = tm.getIntProperty("locaLogicalTime");
                if(remoteLogicalTime > locaLogicalTime) {
                    locaLogicalTime = remoteLogicalTime;
                }
                double[] numbersBC = (double[]) tm.getObject();
                locaLogicalTime++;
                sendMessage("Proces C: odebrano dane z procesu B, czas: " + locaLogicalTime);
                sleepRandomTime();


                tm = (ObjectMessage) consumerDC.receive();
                if (tm == null) {
                    break;
                }
                remoteLogicalTime = tm.getIntProperty("locaLogicalTime");
                if(remoteLogicalTime > locaLogicalTime) {
                    locaLogicalTime = remoteLogicalTime;
                }
                double[] numbersDC = (double[]) tm.getObject();
                locaLogicalTime++;
                sendMessage("Proces C: odebrano dane z procesu D, czas: " + locaLogicalTime);
                sleepRandomTime();

                double[] numbersCA = new double[100];
                for (int i = 0; i < numbersBC.length; i++) {
                    numbersCA[i] = numbersBC[i] - numbersDC[i];
                }
                locaLogicalTime++;
                sendMessage("Proces C: obliczono roznice miedzy danymi z procesow B i D, czas: " + locaLogicalTime);
                sleepRandomTime();

                ObjectMessage objectMessage = session.createObjectMessage(numbersCA);
                objectMessage.setIntProperty("locaLogicalTime", locaLogicalTime);
                producerCA.send(objectMessage);
                locaLogicalTime++;
                sendMessage("Proces C: wysłano dane do procesu A, czas: " + locaLogicalTime);
                sleepRandomTime();

                locaLogicalTime++;
                sendMessage("Proces C: powrot do kroku 1, czas: " + locaLogicalTime);
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
