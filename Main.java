import java.util.*;

public class Main {
    static final Scanner sc = new Scanner(System.in);
    static final Random rnd = new Random();

    static final double ISLAND_HOP_PP = 160;
    static final double CITY_TOUR_PP = 45;
    static final double SHARKS_PP = 120;
    static final double DOLPHINS_PP = 150;
    static final double SPA_PP = 95;

    static final String PASSPORT_CONFIRM_PHONE = "+49-111-222";

    static final String BANK_NAME = "Oceanic Trust Bank";
    static final String ACCOUNT_NAME = "Maldives Dream Stay Booking";
    static final String IBAN = "DE89 3704 0044 0532 0130 00";
    static final String SWIFT_BIC = "OTBKDEFFXXX";

    static class Hotel {
        String id, name, atoll, transfer;
        int stars, transferMin;
        double basePerNight, transferFeePP;
        List<String> complaints;

        Hotel(String id, String name, String atoll, int stars, double basePerNight,
              String transfer, int transferMin, double transferFeePP, List<String> complaints) {
            this.id = id;
            this.name = name;
            this.atoll = atoll;
            this.stars = stars;
            this.basePerNight = basePerNight;
            this.transfer = transfer;
            this.transferMin = transferMin;
            this.transferFeePP = transferFeePP;
            this.complaints = complaints == null ? new ArrayList<String>() : new ArrayList<String>(complaints);
        }

        String line() {
            return String.format("[%s] %s | Stars: %d | %s | $%.2f/night",
                    id, name, stars, atoll, basePerNight);
        }
    }

    static class Reservation {
        Hotel hotel;

        String bookingCode;
        String contactName;
        String email;
        String checkInDate;

        int nights;
        int adults;
        int kids;

        String villaType;
        String view;
        String mealPlan;

        String promoCode;
        String promoMessage;

        int islandHopPeople;
        int cityTourPeople;
        int sharksPeople;
        int dolphinsPeople;
        int spaPeople;

        List<String> guestNames = new ArrayList<String>();

        double roomSubtotal;
        double transferTotal;
        double subtotal;
        double ecoTax;

        double autoFamilyDisc;
        double longStayDisc;
        double promoDisc;
        double discountsTotal;

        double addOnsSubtotal;
        double addOnsFamilyDisc;
        double addOnsTotal;

        double serviceCharge;
        double total;

        double deposit;
        double balance;
        boolean depositPaid;
        String paymentMethod;
        String maskedCard;
        String transactionId;
    }

    public static void main(String[] args) {
        List<Hotel> allHotels = seedHotels();
        printBanner();

        int stars = readInt("Choose hotel Stars (1-5): ", 1, 5);
        List<Hotel> options = hotelsByStars(allHotels, stars);

        if (options.isEmpty()) {
            System.out.println("No hotels found for that star rating.");
            return;
        }

        System.out.println();
        System.out.println("Available hotels (Stars: " + stars + " only)");
        System.out.println("--------------------------------");
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ") " + options.get(i).line());
        }

        int pick = readInt("Select hotel (1-" + options.size() + "): ", 1, options.size());
        Hotel chosen = options.get(pick - 1);
        showHotelDetails(chosen);

        Reservation r = new Reservation();
        r.hotel = chosen;
        r.bookingCode = makeBookingCode(chosen.id);

        System.out.println();
        System.out.println("Guest information");
        System.out.println("-----------------");
        r.contactName = readFullName("Main contact full name (name and surname): ");
        r.email = readEmail("Email: ");

        System.out.println();
        System.out.println("Stay details");
        System.out.println("------------");
        r.checkInDate = readDateDMY("Check-in date (DD-MM-YYYY): ");
        r.nights = readInt("Nights (1-21): ", 1, 21);

        System.out.println();
        System.out.println("Group details");
        System.out.println("-------------");
        r.adults = readInt("Adults (1-6): ", 1, 6);
        r.kids = readInt("Kids (0-6): ", 0, 6);

        System.out.println();
        System.out.println("Room setup");
        System.out.println("----------");
        r.villaType = choose("Villa type", new String[]{"Garden villa", "Beach villa", "Water villa"});
        r.view = choose("View", new String[]{"Lagoon view", "Sunset view", "Resort view"});
        r.mealPlan = choose("Meal plan", new String[]{"No meal plan", "Breakfast", "All inclusive"});

        System.out.println();
        System.out.println("Discount codes");
        System.out.println("--------------");
        System.out.println("FAMILYTRIP  15% off subtotal  requires at least 2 adults and at least 2 kids");
        System.out.println("GATEAWAY     8% off subtotal  requires exactly 2 total guests");
        r.promoCode = readLine("Enter discount code or press Enter to skip: ").trim();

        computeBaseAndDiscounts(r);

        System.out.println();
        System.out.println("Special offers");
        System.out.println("--------------");
        showOffersTable(r.adults + r.kids);

        if (readYesNo("Do you want to choose any of the special offers (yes/no): ")) {
            pickAddOns(r);
        } else {
            System.out.println("No special offers selected.");
        }

        computeTotals(r);

        System.out.println();
        printSummary(r);

        System.out.println();
        collectGuestNames(r);

        System.out.println();
        System.out.println("PLEASE SEND PASSPORT PHOTOS FOR EACH MEMBER TO CONFIRM YOUR RESERVATION AT THE HOTEL");
        System.out.println("Send to: " + PASSPORT_CONFIRM_PHONE);
        for (int i = 0; i < r.guestNames.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + r.guestNames.get(i));
        }

        System.out.println();
        if (!readYesNo("Proceed with reservation (yes/no): ")) {
            System.out.println();
            System.out.println("Reservation canceled");
            return;
        }

        r.deposit = round2(r.total * 0.20);
        r.balance = round2(r.total - r.deposit);

        handlePayment(r);

        System.out.println();
        printReceipt(r);

        System.out.println();
        printArrivalAndPackingInfo();

        System.out.println();
        printBankTransferDetails(r);

        System.out.println();
        System.out.println("Thank you for booking. Enjoy the Maldives.");
    }

    static void showOffersTable(int totalGuests) {
        System.out.println("Offers list (per person)");
        System.out.println("------------------------");
        System.out.println("1) Island hopping day trip       $160");
        System.out.println("2) City tour in Malé             $45");
        System.out.println("3) Swim with friendly sharks     $120");
        System.out.println("4) Swim with dolphins            $150");
        System.out.println("5) Spa session                   $95");
        System.out.println();
        System.out.println("Max people you can assign per offer: " + totalGuests);
    }

    static void computeBaseAndDiscounts(Reservation r) {
        int people = r.adults + r.kids;

        r.roomSubtotal = round2(r.hotel.basePerNight * r.nights);
        r.transferTotal = round2(r.hotel.transferFeePP * people);
        r.subtotal = round2(r.roomSubtotal + r.transferTotal);

        r.autoFamilyDisc = 0;
        r.longStayDisc = 0;
        r.promoDisc = 0;
        r.discountsTotal = 0;

        if (r.adults == 2 && r.kids == 2) r.autoFamilyDisc = round2(r.subtotal * 0.10);
        if (r.nights >= 7) r.longStayDisc = round2(r.subtotal * 0.07);

        r.promoMessage = "No promo code entered.";
        String code = (r.promoCode == null) ? "" : r.promoCode.trim().toUpperCase();

        if (!code.isEmpty()) {
            int totalGuests = r.adults + r.kids;

            if (code.equals("FAMILYTRIP")) {
                if (r.adults >= 2 && r.kids >= 2) {
                    r.promoDisc = round2(r.subtotal * 0.15);
                    r.promoMessage = "Promo applied: FAMILYTRIP (15%)";
                } else {
                    r.promoMessage = "Promo not applied: FAMILYTRIP requires at least 2 adults and at least 2 kids.";
                }
            } else if (code.equals("GATEAWAY")) {
                if (totalGuests == 2) {
                    r.promoDisc = round2(r.subtotal * 0.08);
                    r.promoMessage = "Promo applied: GATEAWAY (8%)";
                } else {
                    r.promoMessage = "Promo not applied: GATEAWAY is only for exactly 2 total guests.";
                }
            } else {
                r.promoMessage = "Promo not applied: unknown code.";
            }
        }

        r.discountsTotal = round2(r.autoFamilyDisc + r.longStayDisc + r.promoDisc);
        r.ecoTax = round2(6.0 * people * r.nights);

        System.out.println();
        System.out.println("Discount status");
        System.out.println("--------------");
        System.out.println(r.promoMessage);
        if (r.autoFamilyDisc > 0) System.out.println("Auto discount applied: Family (10%)");
        if (r.longStayDisc > 0) System.out.println("Auto discount applied: Long stay 7+ nights (7%)");
        if (r.autoFamilyDisc == 0 && r.longStayDisc == 0 && r.promoDisc == 0) System.out.println("No discounts applied");
    }

    static void computeTotals(Reservation r) {
        double add = 0;
        add += r.islandHopPeople * ISLAND_HOP_PP;
        add += r.cityTourPeople * CITY_TOUR_PP;
        add += r.sharksPeople * SHARKS_PP;
        add += r.dolphinsPeople * DOLPHINS_PP;
        add += r.spaPeople * SPA_PP;

        r.addOnsSubtotal = round2(add);

        r.addOnsFamilyDisc = 0;
        if (r.adults >= 2 && r.kids >= 2 && r.addOnsSubtotal > 0) {
            r.addOnsFamilyDisc = round2(r.addOnsSubtotal * 0.05);
        }
        r.addOnsTotal = round2(r.addOnsSubtotal - r.addOnsFamilyDisc);

        double afterDiscount = Math.max(0, r.subtotal - r.discountsTotal);
        double basePlusAddOns = afterDiscount + r.addOnsTotal;

        r.serviceCharge = round2(basePlusAddOns * 0.10);
        r.total = round2(basePlusAddOns + r.ecoTax + r.serviceCharge);
    }

    static void pickAddOns(Reservation r) {
        int totalGuests = r.adults + r.kids;

        while (true) {
            System.out.println();
            int choice = readInt("Pick an offer number (1-5) or 0 to finish: ", 0, 5);
            if (choice == 0) break;

            int people = readInt("How many people for this offer (1-" + totalGuests + "): ", 1, totalGuests);

            if (choice == 1) r.islandHopPeople += people;
            if (choice == 2) r.cityTourPeople += people;
            if (choice == 3) r.sharksPeople += people;
            if (choice == 4) r.dolphinsPeople += people;
            if (choice == 5) r.spaPeople += people;

            System.out.println();
            System.out.println("Current add-ons");
            printAddOns(r);

            System.out.println();
            if (!readYesNo("Add another offer (yes/no): ")) break;
        }
    }

    static void printAddOns(Reservation r) {
        boolean any = false;
        if (r.islandHopPeople > 0) { System.out.println("  • Island hopping people: " + r.islandHopPeople); any = true; }
        if (r.cityTourPeople > 0) { System.out.println("  • City tour people: " + r.cityTourPeople); any = true; }
        if (r.sharksPeople > 0) { System.out.println("  • Swim with friendly sharks people: " + r.sharksPeople); any = true; }
        if (r.dolphinsPeople > 0) { System.out.println("  • Swim with dolphins people: " + r.dolphinsPeople); any = true; }
        if (r.spaPeople > 0) { System.out.println("  • Spa session people: " + r.spaPeople); any = true; }
        if (!any) System.out.println("  • None");
    }

    static void handlePayment(Reservation r) {
        System.out.println();
        System.out.println("Payment options");
        System.out.println("--------------");
        System.out.printf("Total: $%.2f%n", r.total);
        System.out.printf("Deposit required to secure booking: $%.2f%n", r.deposit);
        System.out.printf("Remaining balance: $%.2f%n", r.balance);
        System.out.println();
        System.out.println("1 Pay deposit now by card");
        System.out.println("2 Pay by bank transfer later (deposit not paid now)");

        int choice = readInt("Choose payment method (1-2): ", 1, 2);

        if (choice == 1) {
            r.paymentMethod = "CARD";

            System.out.println();
            System.out.println("Card payment");
            System.out.println("------------");
            String card = readDigits("Card number (16 digits): ", 16);
            readExpiry("Expiry (MM/YY): ");
            readDigits("CVV (3 digits): ", 3);

            r.depositPaid = true;
            r.maskedCard = "**** **** **** " + card.substring(12);
            r.transactionId = "TX-" + (100000 + rnd.nextInt(900000));

            System.out.println();
            System.out.println("Payment approved");
            System.out.printf("Deposit paid now: $%.2f%n", r.deposit);
        } else {
            r.paymentMethod = "BANK";
            r.depositPaid = false;

            System.out.println();
            System.out.println("Bank transfer selected");
            System.out.println("Deposit is NOT paid now.");
            System.out.println("You must transfer the deposit to secure the reservation.");
        }
    }

    static void printSummary(Reservation r) {
        int people = r.adults + r.kids;

        System.out.println("Price summary");
        System.out.println("-------------");
        System.out.println("Booking code: " + r.bookingCode);
        System.out.println("Hotel: " + r.hotel.name + " | Stars: " + r.hotel.stars + " | " + r.hotel.atoll);
        System.out.println("Check-in: " + r.checkInDate + "  Nights: " + r.nights);
        System.out.println("Guests: " + people + " (" + r.adults + " adults, " + r.kids + " kids)");
        System.out.println("Room: " + r.villaType + "  View: " + r.view + "  Meal: " + r.mealPlan);

        System.out.println();
        System.out.printf("Room subtotal: $%.2f%n", r.roomSubtotal);
        System.out.printf("Transfer total: $%.2f%n", r.transferTotal);
        System.out.printf("Subtotal: $%.2f%n", r.subtotal);

        System.out.println();
        System.out.println("Discount breakdown");
        System.out.println("------------------");
        if (r.autoFamilyDisc > 0) System.out.printf("Auto family discount (10%%): -$%.2f%n", r.autoFamilyDisc);
        if (r.longStayDisc > 0) System.out.printf("Long stay discount (7%%):    -$%.2f%n", r.longStayDisc);
        if (r.promoDisc > 0) System.out.printf("Promo discount:             -$%.2f%n", r.promoDisc);
        if (r.autoFamilyDisc == 0 && r.longStayDisc == 0 && r.promoDisc == 0) System.out.println("No discounts applied");
        System.out.printf("Discounts total: -$%.2f%n", r.discountsTotal);

        System.out.println();
        System.out.println("Add-ons");
        System.out.println("------");
        printAddOns(r);
        System.out.printf("Add-ons subtotal: $%.2f%n", r.addOnsSubtotal);
        if (r.addOnsFamilyDisc > 0) System.out.printf("Add-ons family discount (5%%): -$%.2f%n", r.addOnsFamilyDisc);
        System.out.printf("Add-ons total: $%.2f%n", r.addOnsTotal);

        System.out.println();
        System.out.printf("Eco tax: $%.2f%n", r.ecoTax);
        System.out.printf("Service charge: $%.2f%n", r.serviceCharge);
        System.out.println("--------------------------------");
        System.out.printf("TOTAL: $%.2f%n", r.total);
    }

    static void printReceipt(Reservation r) {
        System.out.println("================ RECEIPT ================");
        System.out.println("Booking code: " + r.bookingCode);
        System.out.println("Contact: " + r.contactName);
        System.out.println("Email: " + r.email);
        System.out.println("Hotel: " + r.hotel.name + " (Stars: " + r.hotel.stars + ")");
        System.out.println("Check-in: " + r.checkInDate + "  Nights: " + r.nights);
        System.out.printf("Total cost: $%.2f%n", r.total);

        System.out.println();
        System.out.println("Payment status");
        System.out.println("--------------");
        if (r.depositPaid) {
            System.out.println("Payment method: Card");
            System.out.println("Card: " + r.maskedCard);
            System.out.println("Transaction ID: " + r.transactionId);
            System.out.printf("Deposit paid online: $%.2f%n", r.deposit);
            System.out.printf("Balance due at check-in: $%.2f%n", r.balance);
            System.out.println("Reservation status: CONFIRMED");
        } else {
            System.out.println("Payment method: Bank transfer");
            System.out.printf("Deposit not paid yet: $%.2f%n", r.deposit);
            System.out.printf("Balance after deposit: $%.2f%n", r.balance);
            System.out.println("Reservation status: PENDING PAYMENT");
        }
        System.out.println("=========================================");
    }

    static void printArrivalAndPackingInfo() {
        System.out.println("Arrival and important information");
        System.out.println("--------------------------------");
        System.out.println("Airport: Velana International Airport (MLE)");
        System.out.println("After landing: luggage → arrivals hall → resort counter → transfer");
        System.out.println();
        System.out.println("Packing tips");
        System.out.println("• Light clothing and comfortable sandals");
        System.out.println("• Reef-safe sunscreen and sunglasses");
        System.out.println("• Swimwear and a light cover-up");
        System.out.println("• Power adapter and phone charger");
        System.out.println("• Optional: snorkel mask");
    }

    static void printBankTransferDetails(Reservation r) {
        System.out.println("Bank transfer details");
        System.out.println("---------------------");
        System.out.println("Bank: " + BANK_NAME);
        System.out.println("Account name: " + ACCOUNT_NAME);
        System.out.println("IBAN: " + IBAN);
        System.out.println("SWIFT/BIC: " + SWIFT_BIC);
        System.out.println("Payment reference: " + r.bookingCode);
        System.out.printf("Deposit amount to transfer: $%.2f%n", r.deposit);
    }

    static void collectGuestNames(Reservation r) {
        int total = r.adults + r.kids;
        r.guestNames.clear();

        System.out.println("Enter full name and surname for each guest");
        for (int i = 1; i <= total; i++) {
            r.guestNames.add(readFullName("Guest " + i + " full name: "));
        }
    }

    static String readFullName(String msg) {
        while (true) {
            System.out.print(msg);
            String s = sc.nextLine().trim().replaceAll("\\s+", " ");
            if (s.isEmpty()) {
                System.out.println("Input cannot be empty");
                continue;
            }
            if (s.split(" ").length < 2) {
                System.out.println("Please enter both name and surname");
                continue;
            }
            return s;
        }
    }

    static List<Hotel> hotelsByStars(List<Hotel> all, int stars) {
        List<Hotel> res = new ArrayList<Hotel>();
        for (Hotel h : all) if (h.stars == stars) res.add(h);
        int limit = Math.min(3, res.size());
        return res.subList(0, limit);
    }

    static void showHotelDetails(Hotel h) {
        System.out.println();
        System.out.println("Hotel details");
        System.out.println("-------------");
        System.out.println(h.line());

        System.out.println();
        System.out.println("Guest feedback");
        if (h.complaints.isEmpty()) {
            System.out.println("No complaints reported");
            System.out.println("Positive mentions: friendly staff, clean villas, beautiful lagoon");
        } else {
            for (String c : h.complaints) System.out.println("• " + c);
        }

        System.out.println();
        System.out.println("Transfer from MLE");
        System.out.println("Type: " + h.transfer);
        System.out.println("Time: " + h.transferMin + " minutes");
        System.out.printf("Fee per person: $%.2f%n", h.transferFeePP);
    }

    static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v < min || v > max) {
                    System.out.println("Enter a number from " + min + " to " + max);
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number");
            }
        }
    }

    static boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim().toLowerCase();
            if (s.equals("yes") || s.equals("y")) return true;
            if (s.equals("no") || s.equals("n")) return false;
            System.out.println("Type yes or no");
        }
    }

    static String readEmail(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (s.contains("@") && s.contains(".") && s.length() >= 5) return s;
            System.out.println("Enter a valid email");
        }
    }

    static String readDateDMY(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (s.length() == 10 && s.charAt(2) == '-' && s.charAt(5) == '-') {
                String dd = s.substring(0, 2), mm = s.substring(3, 5), yy = s.substring(6, 10);
                if (digits(dd) && digits(mm) && digits(yy)) {
                    int d = Integer.parseInt(dd), m = Integer.parseInt(mm), y = Integer.parseInt(yy);
                    if (y >= 2020 && y <= 2099 && m >= 1 && m <= 12 && d >= 1 && d <= 31) return s;
                }
            }
            System.out.println("Use DD-MM-YYYY (example 01-05-2026)");
        }
    }

    static boolean digits(String s) {
        for (int i = 0; i < s.length(); i++) if (!Character.isDigit(s.charAt(i))) return false;
        return true;
    }

    static String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    static String choose(String title, String[] options) {
        System.out.println();
        System.out.println(title);
        for (int i = 0; i < options.length; i++) System.out.println((i + 1) + ") " + options[i]);
        int c = readInt("Choice: ", 1, options.length);
        return options[c - 1];
    }

    static String readDigits(String prompt, int len) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim().replace(" ", "");
            if (s.length() != len || !digits(s)) {
                System.out.println("Enter exactly " + len + " digits");
                continue;
            }
            return s;
        }
    }

    static String readExpiry(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (s.length() == 5 && s.charAt(2) == '/' && digits(s.substring(0, 2)) && digits(s.substring(3, 5))) {
                int m = Integer.parseInt(s.substring(0, 2));
                if (m >= 1 && m <= 12) return s;
            }
            System.out.println("Format MM/YY (example 09/29)");
        }
    }

    static double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }

    static String makeBookingCode(String hotelId) {
        char a = (char) ('A' + rnd.nextInt(26));
        char b = (char) ('A' + rnd.nextInt(26));
        int num = 10000 + rnd.nextInt(90000);
        return hotelId + "-" + a + b + "-" + num;
    }

    static void printBanner() {
        System.out.println("============================================================");
        System.out.println("                 MALDIVES DREAM STAY BOOKING                ");
        System.out.println("============================================================");
        System.out.println("Choose Stars, pick a resort, read reviews, reserve and pay");
        System.out.println("Villa types, meal plans, transfers, discount codes, add-ons");
        System.out.println("============================================================");
    }

    static List<Hotel> seedHotels() {
        List<Hotel> h = new ArrayList<Hotel>();

        h.add(new Hotel("M51", "Velassu Pearl Lagoon", "North Malé", 5, 520, "Speedboat", 35, 95, Collections.<String>emptyList()));
        h.add(new Hotel("M52", "Sunset Mirihi Villas", "Ari", 5, 610, "Seaplane", 45, 280, Arrays.asList("Limited late-night dining")));
        h.add(new Hotel("M53", "Azure Drift Private Island", "Baa", 5, 690, "Seaplane", 35, 300, Collections.<String>emptyList()));

        h.add(new Hotel("M41", "Coral Path Retreat", "South Malé", 4, 340, "Speedboat", 25, 75, Collections.<String>emptyList()));
        h.add(new Hotel("M42", "Palm Ribbon Lagoon Hotel", "Lhaviyani", 4, 310, "Domestic+Speedboat", 70, 220, Arrays.asList("Wi-Fi can be slower during storms")));
        h.add(new Hotel("M43", "Lagoon Crown Sands", "Raa", 4, 325, "Domestic+Speedboat", 80, 210, Collections.<String>emptyList()));

        h.add(new Hotel("M31", "Reefline Island Stay", "Vaavu", 3, 190, "Speedboat", 60, 110, Arrays.asList("Longer walk to some rooms")));
        h.add(new Hotel("M32", "Lagoon Breeze Budget Resort", "Thaa", 3, 170, "Domestic+Speedboat", 85, 200, Collections.<String>emptyList()));
        h.add(new Hotel("M33", "Coconut Shore Stay", "Meemu", 3, 180, "Speedboat", 55, 120, Collections.<String>emptyList()));

        h.add(new Hotel("M21", "Seashell Simple Lodge", "Haa Alif", 2, 120, "Domestic+Speedboat", 95, 180, Arrays.asList("Basic amenities")));
        h.add(new Hotel("M22", "Island Pocket Inn", "Laamu", 2, 110, "Domestic+Speedboat", 90, 175, Arrays.asList("Occasional noise nearby")));
        h.add(new Hotel("M23", "Blue Jetty Guest Rooms", "Dhaalu", 2, 115, "Domestic+Speedboat", 88, 170, Collections.<String>emptyList()));

        h.add(new Hotel("M11", "Palm Shade Guesthouse", "Addu", 1, 75, "Domestic+Speedboat", 100, 160, Arrays.asList("Small rooms")));
        h.add(new Hotel("M12", "Harbor Corner Hostel", "North Malé", 1, 65, "Speedboat", 40, 60, Collections.<String>emptyList()));
        h.add(new Hotel("M13", "Sunrise Budget Rooms", "Gaafu", 1, 70, "Domestic+Speedboat", 92, 150, Arrays.asList("Limited housekeeping")));

        return h;
    }
}


 
 
 
 
 
 
 
 
 
 
 
 
 
 