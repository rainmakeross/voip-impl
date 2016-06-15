import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.Utils;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import org.apache.log4j.spi.LoggerFactory;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;


public class Library implements SipListener {
    Logger logger = Logger.getLogger(Library.class.getName());

    
    // Objects used to communicate to the JAIN SIP API.
    SipFactory sipFactory;          // Used to access the SIP API.
    SipStack sipStack;              // The SIP stack.
    SipProvider sipProvider;        // Used to send SIP messages.
    MessageFactory messageFactory;  // Used to create SIP message factory.
    HeaderFactory headerFactory;    // Used to create SIP headers.
    AddressFactory addressFactory;  // Used to create SIP URIs.
    ListeningPoint listeningPoint;  // SIP listening IP address/port.
    Properties properties;          // Other properties.

    // Objects keeping local configuration.
    String ip;                      // The local IP address.
    int port = 6060;                // The local port.
    String protocol = "tcp";        // The local protocol (UDP).
    int tag = (new Random()).nextInt(); // The local tag.
    Address contactAddress;         // The contact address.
    ContactHeader contactHeader;    // The contact header.

    //Destination SIP details
    String destinationSip = "sip:195607@toronto.voip.ms:5080";

    String username = "195607";
    String server = "toronto.voip.ms";

    // Required for response
    private ClientTransaction inviteTid;
    long invco = 1;
    private Dialog dialog;



    public static void main(String[] args) throws Exception {
        Library lib = new Library();
        lib.init();
        lib.register2();
    }

    public void register() {
        try {
            ArrayList viaHeaders = new ArrayList();
            ViaHeader viaHeader = this.headerFactory.createViaHeader(this.ip,
                this.port, "tcp", null);
            viaHeaders.add(viaHeader);
            // The "Max-Forwards" header.
            MaxForwardsHeader maxForwardsHeader = this.headerFactory
                .createMaxForwardsHeader(70);
            // The "Call-Id" header.
            CallIdHeader callIdHeader = this.sipProvider.getNewCallId();
            // The "CSeq" header.
            CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(1L,
                "REGISTER");

            Address fromAddress = addressFactory.createAddress("sip:" + username + '@' + server);

            FromHeader fromHeader = this.headerFactory.createFromHeader(
                fromAddress, String.valueOf(this.tag));
            // The "To" header.
            ToHeader toHeader = this.headerFactory.createToHeader(fromAddress,
                null);

            // this.contactHeader = this.headerFactory
            // .createContactHeader(contactAddress);

            Request request = this.messageFactory.createRequest("REGISTER sip:"
            + server + " SIP/2.0\r\n\r\n");
            request.addHeader(callIdHeader);
            request.addHeader(cSeqHeader);
            request.addHeader(fromHeader);
            request.addHeader(toHeader);
            request.addHeader(maxForwardsHeader);
            request.addHeader(viaHeader);
            request.addHeader(contactHeader);
            this.sipProvider.sendRequest(request);
/*
            if (response != null) {
                retry = true;
                AuthorizationHeader authHeader = Utils.makeAuthHeader(headerFactory, response,
                    request, username, password);
                request.addHeader(authHeader);
            }
            inviteTid = sipProvider.getNewClientTransaction(request);
            // send the request out.
            inviteTid.sendRequest();
            this.dialog = inviteTid.getDialog();
            // Send the request statelessly through the SIP provider.
            //            this.sipProvider.sendRequest(request);
*/

            // Display the message in the text area.
            logger.warning("Request sent:\n" + request.toString() + "\n\n");
        } catch (Exception e) {
            // If an error occurred, display the error.
            e.printStackTrace();
            logger.warning("Request sent failed: " + e.getMessage() + "\n");
        }
    }

    public void register2() {
        try {
            // Get the destination address from the text field.
            Address addressTo = this.addressFactory.createAddress(destinationSip);
            // Create the request URI for the SIP message.
            javax.sip.address.URI requestURI = addressTo.getURI();

            // Create the SIP message headers.

            // The "Via" headers.
            ArrayList viaHeaders = new ArrayList();
            ViaHeader viaHeader = this.headerFactory.createViaHeader(this.ip, this.port, "tcp", null);
            viaHeaders.add(viaHeader);
            // The "Max-Forwards" header.
            MaxForwardsHeader maxForwardsHeader = this.headerFactory.createMaxForwardsHeader(70);
            // The "Call-Id" header.
            CallIdHeader callIdHeader = this.sipProvider.getNewCallId();
            // The "CSeq" header.
            CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(1L,"REGISTER");
            // The "From" header.
            FromHeader fromHeader = this.headerFactory.createFromHeader(this.contactAddress, String.valueOf(this.tag));
            // The "To" header.
            ToHeader toHeader = this.headerFactory.createToHeader(addressTo, null);

            // Create the REGISTER request.
            Request request = this.messageFactory.createRequest(
                requestURI,
                "REGISTER",
                callIdHeader,
                cSeqHeader,
                fromHeader,
                toHeader,
                viaHeaders,
                maxForwardsHeader);
            // Add the "Contact" header to the request.
            request.addHeader(contactHeader);

            // Send the request statelessly through the SIP provider.
            this.sipProvider.sendRequest(request);

            // Display the message in the text area.
            System.out.println(
                "Request sent:\n" + request.toString() + "\n\n");
        }
        catch(Exception e) {
            // If an error occurred, display the error.
            e.printStackTrace();
            System.out.println("Request sent failed: " + e.getMessage() + "\n");
        }
    }

    private void init() {
        try {
            // Get the local IP address.
            this.ip = "192.168.0.105";
            // Create the SIP factory and set the path name.
            this.sipFactory = SipFactory.getInstance();
            this.sipFactory.setPathName("gov.nist");
            // Create and set the SIP stack properties.
            this.properties = new Properties();
            this.properties.setProperty("javax.sip.STACK_NAME", "stack");
            // Create the SIP stack.
            this.sipStack = this.sipFactory.createSipStack(this.properties);
            // Create the SIP message factory.
            this.messageFactory = this.sipFactory.createMessageFactory();
            // Create the SIP header factory.
            this.headerFactory = this.sipFactory.createHeaderFactory();
            // Create the SIP address factory.
            this.addressFactory = this.sipFactory.createAddressFactory();
            // Create the SIP listening point and bind it to the local IP address, port and protocol.
            this.listeningPoint = this.sipStack.createListeningPoint(this.ip, this.port, this.protocol);
            // Create the SIP provider.
            this.sipProvider = this.sipStack.createSipProvider(this.listeningPoint);
            // Add our application as a SIP listener.
            this.sipProvider.addSipListener(this);
            // Create the contact address used for all SIP messages.
            this.contactAddress = this.addressFactory.createAddress("sip:" + this.ip + ":" + this.port);
            // Create the contact header used for all SIP messages.
            this.contactHeader = this.headerFactory.createContactHeader(contactAddress);

            // Display the local IP address and port in the text area.
            System.out.println("Local address: " + this.ip + ":" + this.port + "\n");
        }
        catch(Exception e) {
            // If an error occurs, display an error message box and exit.
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override public void processRequest(RequestEvent requestEvent) {

    }

    @Override public void processResponse(ResponseEvent responseEvent) {
        System.out.println("Got a response");
        Response response = (Response) responseEvent.getResponse();
        ClientTransaction tid = responseEvent.getClientTransaction();
        CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

        System.out.println("Response received : Status Code = "
            + response.getStatusCode() + " " + cseq);
        if (tid == null) {
            System.out.println("Stray response -- dropping ");
            return;
        }
        System.out.println("transaction state is " + tid.getState());
        System.out.println("Dialog = " + tid.getDialog());
        System.out.println("Dialog State is " + tid.getDialog().getState());

        try {
            if (response.getStatusCode() == Response.OK) {
                if (cseq.getMethod().equals(Request.INVITE)) {
                    Dialog dialog = inviteTid.getDialog();
                    Request ackRequest = dialog.createAck( cseq.getSeqNumber() );
                    System.out.println("Sending ACK");
                    dialog.sendAck(ackRequest);
                } else if (cseq.getMethod().equals(Request.CANCEL)) {
                    if (dialog.getState() == DialogState.CONFIRMED) {
                        // oops cancel went in too late. Need to hang up the
                        // dialog.
                        System.out
                            .println("Sending BYE -- cancel went in too late !!");
                        Request byeRequest = dialog.createRequest(Request.BYE);
                        ClientTransaction ct = sipProvider
                            .getNewClientTransaction(byeRequest);
                        dialog.sendRequest(ct);
                    }
                }
            } else if (response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED
                || response.getStatusCode() == Response.UNAUTHORIZED) {
                AuthenticationHelper authenticationHelper =
                    ((SipStackExt) sipStack).getAuthenticationHelper(new AccountManagerImpl(), headerFactory);

                inviteTid = authenticationHelper.handleChallenge(response, tid, sipProvider, 5);

                inviteTid.sendRequest();

                invco++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    @Override public void processTimeout(TimeoutEvent timeoutEvent) {

    }

    @Override public void processIOException(IOExceptionEvent exceptionEvent) {

    }

    @Override public void processTransactionTerminated(
        TransactionTerminatedEvent transactionTerminatedEvent) {

    }

    @Override public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {

    }
}
