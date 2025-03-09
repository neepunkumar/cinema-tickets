package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */


    private static final int ADULT_TICKET_COST = 25;
    private static final int CHILD_TICKET_COST = 15;
    private static final int MAX_TICKETS = 25;


    private final TicketPaymentService paymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService seatReservationService) {
        this.paymentService = paymentService;
        this.seatReservationService = seatReservationService;
    }



    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        validateAccountId(accountId);

        int totalTickets = 0 ;
        int totalCost = 0;
        int adultTickets = 0;
        int childTickets = 0;
        int infantTickets = 0;

        for (TicketTypeRequest request: ticketTypeRequests) {
            switch(request.getTicketType()) {
                
                case ADULT:
                    adultTickets += request.getNoOfTickets();
                    totalCost += request.getNoOfTickets() * ADULT_TICKET_COST;
                    break;
                
                case CHILD:
                    childTickets += request.getNoOfTickets();
                    totalCost += request.getNoOfTickets() * CHILD_TICKET_COST;
                    break;

                case INFANT:
                    infantTickets += request.getNoOfTickets();
                    break;
            }
            totalTickets += request.getNoOfTickets();

        }

        validateTicketPurchase(totalTickets, adultTickets, childTickets, infantTickets);

        paymentService.makePayment(accountId, totalCost);

        int totalSeats = adultTickets + childTickets;
        seatReservationService.reserveSeat(accountId, adultTickets + childTickets);

        

        

        

    }

    private void validateAccountId(Long accountId) throws InvalidPurchaseException {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Invalid account id");
        }
    }

    private void validateTicketPurchase(int totalTickets, int adultTickets, int childTickets, int infantTickets) throws InvalidPurchaseException {
        if (totalTickets > MAX_TICKETS) {
            throw new InvalidPurchaseException("Maximum of 25 tickets can be purchased at a time");
        }

        if ((childTickets > 0 || infantTickets > 0) && adultTickets == 0) {
            throw new InvalidPurchaseException("At least one adult ticket is required for each child or infant ticket");
        }
    }

}
