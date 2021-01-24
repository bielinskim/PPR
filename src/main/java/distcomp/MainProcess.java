package distcomp;

import javax.jms.*;
import java.util.Random;

public class MainProcess extends Thread {
    private final Connection con;
    Session session;
    MessageConsumer consumerR, consumerF;
    MessageProducer[] producers = new MessageProducer[3];
    Random rand;
    int response = 0;

    public MainProcess() throws JMSException {
        rand = new Random();
        ConnectionFactory factory = JmsProvider.getConnectionFactory();
        this.con = factory.createConnection();
        con.start();

        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);


        Queue queue = session.createQueue("R");
        consumerR = session.createConsumer(queue);

        queue = session.createQueue("F");
        consumerF = session.createConsumer(queue);

        Queue queue1 = session.createQueue("0");
        producers[0] = session.createProducer(queue1);


        Queue queue2 = session.createQueue("1");
        producers[1] = session.createProducer(queue2);

        Queue queue3 = session.createQueue("2");
        producers[2] = session.createProducer(queue3);



    }

    private void sleepRandomTime() {
        try {
            Thread.sleep((rand.nextInt(4) + 1) * 1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }



    @Override
    public void run() {
        try {

            while (true) {


                TextMessage message = (TextMessage) consumerR.receive();
                if (message == null) {
                    break;
                }
                String res = message.getText();

                System.out.println("Main Process: " + res);


                sleepRandomTime();
                int producerId = message.getIntProperty("producer");
                message = session.createTextMessage("response nr " + response++);
                producers[producerId].send(message);


                message = (TextMessage) consumerF.receive();
                if (message == null) {
                    break;
                }
                res = message.getText();
                System.out.println("Main Process: " + res);



            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void destroy() throws JMSException {
        con.close();
    }
}
