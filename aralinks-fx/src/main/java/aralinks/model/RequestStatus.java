package aralinks.model;

/** Replaces Request.status: String from the UML, so a typo can't create an invalid status. */
public enum RequestStatus { PENDING, ACCEPTED, DECLINED, CANCELLED }
