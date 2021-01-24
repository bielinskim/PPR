package distcomp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;

public class ProcessA extends Thread {

    private final Session session;
    private final Connection con;
    private final MessageProducer producerAB;
    private final MessageProducer producerAD;
    private final MessageConsumer consumerCA;
    MessageProducer topicProducer;
    Path fileName;
    Random rand;
    int locaLogicalTime = 0;
    int remoteLogicalTime = 0;

    //String myProcessId = “P1”; Message m = session.createTextMessage(); m.setStringProperty("SenderId ", myProcessId);

    public ProcessA() throws JMSException {
        fileName = Path.of("data.txt");
        try {
            Files.writeString(fileName, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        rand = new Random();

        ConnectionFactory factory = JmsProvider.getConnectionFactory();
        this.con = factory.createConnection();
        con.start();

        this.session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queueAB = session.createQueue("A-B");
        this.producerAB = session.createProducer(queueAB);
        Queue queueAD = session.createQueue("A-D");
        this.producerAD = session.createProducer(queueAD);
        Queue queueCA = session.createQueue("C-A");
        consumerCA = session.createConsumer(queueCA);
        Topic topic = session.createTopic("ReportTopic");
        topicProducer = session.createProducer(topic);

    }

    private double[] generateNumbers() {
        double[] result = new double[100];
        for (int i = 0; i < 100; i++) {
            result[i] = rand.nextInt(900000) + 100000;
        }
        return result;
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
                double[] numbers = generateNumbers();
                locaLogicalTime++;
                sendMessage("Proces A: wygenerowano losowe liczby, czas: " + locaLogicalTime);
                sleepRandomTime();

                ObjectMessage textMessage = session.createObjectMessage(numbers);
                textMessage.setIntProperty("locaLogicalTime", locaLogicalTime);
                producerAB.send(textMessage);
                locaLogicalTime++;
                sendMessage("Proces A: wysłano dane do procesu B, czas: " + locaLogicalTime);
                sleepRandomTime();

                textMessage = session.createObjectMessage(numbers);
                textMessage.setIntProperty("locaLogicalTime", locaLogicalTime);
                producerAD.send(textMessage);
                locaLogicalTime++;
                sendMessage("Proces A: wysłano dane do procesu D, czas: " + locaLogicalTime);
                sleepRandomTime();

                ObjectMessage om = (ObjectMessage) consumerCA.receive();
                if (om == null) {
                    break;
                }
                remoteLogicalTime = om.getIntProperty("locaLogicalTime");
                if(remoteLogicalTime > locaLogicalTime) {
                    locaLogicalTime = remoteLogicalTime;
                }
                numbers = (double[]) om.getObject();
                try {
                for(int i=0; i<numbers.length; i++) {
                    Files.writeString(fileName, numbers[i] + " ", StandardOpenOption.APPEND);
                }
                Files.writeString(fileName, System.lineSeparator(), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                locaLogicalTime++;
                sendMessage("Proces A: odebrano dane z procesu C i zapisano do pliku, czas: " + locaLogicalTime);
                sleepRandomTime();

                locaLogicalTime++;
                sendMessage("Proces A: powrot do kroku 1, czas: " + locaLogicalTime);
                sleepRandomTime();

            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void destroy() throws JMSException, IOException {
        con.close();
    }
}
