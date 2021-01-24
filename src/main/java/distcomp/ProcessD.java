package distcomp;

import javax.jms.*;
import java.util.Arrays;
import java.util.Random;

public class ProcessD extends Thread {

    private final Connection con;
    Session session;
    MessageConsumer consumerAD;
    MessageProducer producerDC;
    MessageProducer topicProducer;
    Random rand;
    int locaLogicalTime = 0;
    int remoteLogicalTime = 0;

    public ProcessD() throws JMSException {
        rand = new Random();
        ConnectionFactory factory = JmsProvider.getConnectionFactory();
        this.con = factory.createConnection();
        con.start();

        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue queueAD = session.createQueue("A-D");
        consumerAD = session.createConsumer(queueAD);
        Queue queueDC = session.createQueue("D-C");
        producerDC = session.createProducer(queueDC);

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
                ObjectMessage tm = (ObjectMessage) consumerAD.receive();
                if (tm == null) {
                    break;
                }
                remoteLogicalTime = tm.getIntProperty("locaLogicalTime");
                if(remoteLogicalTime > locaLogicalTime) {
                    locaLogicalTime = remoteLogicalTime;
                }
                double[] numbers = (double[]) tm.getObject();
                locaLogicalTime++;
                sendMessage("Proces D: odebrano dane z procesu A, czas: " + locaLogicalTime);
                sleepRandomTime();

                for (int i = 0; i < numbers.length; i++) {
                    numbers[i] = Math.pow(numbers[i], 2);
                }
                locaLogicalTime++;
                sendMessage("Proces D: dane podniesiono do kwadratu, czas: " + locaLogicalTime);
                sleepRandomTime();

                ObjectMessage objectMessage = session.createObjectMessage(numbers);
                objectMessage.setIntProperty("locaLogicalTime", locaLogicalTime);
                producerDC.send(objectMessage);
                locaLogicalTime++;
                sendMessage("Proces D: wysłano dane do procesu C, czas: " + locaLogicalTime);
                sleepRandomTime();

                locaLogicalTime++;
                sendMessage("Proces D: powrot do kroku 1, czas: " + locaLogicalTime);
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
