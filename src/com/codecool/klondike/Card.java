package com.codecool.klondike;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.*;

public class Card extends ImageView {

    private Suit suit;
    private Rank rank;
    private boolean faceDown;

    private Image backFace;
    private Image frontFace;
    private Pile containingPile;
    private DropShadow dropShadow;

    static Image cardBackImage;
    private static final Map<String, Image> cardFaceImages = new HashMap<>();
    public static final int WIDTH = 150;
    public static final int HEIGHT = 215;

    public Card(Suit suit, Rank rank, boolean faceDown) {
        this.suit = suit;
        this.rank = rank;
        this.faceDown = faceDown;
        this.dropShadow = new DropShadow(2, Color.gray(0, 0.75));
        backFace = cardBackImage;
        frontFace = cardFaceImages.get(getShortName());
        setImage(faceDown ? backFace : frontFace);
        setEffect(dropShadow);
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public boolean isFaceDown() {
        return faceDown;
    }

    public String getShortName() {
        return "S" + suit + "R" + rank;
    }

    public DropShadow getDropShadow() {
        return dropShadow;
    }

    public Pile getContainingPile() {
        return containingPile;
    }

    public void setContainingPile(Pile containingPile) {
        this.containingPile = containingPile;
    }

    public void moveToPile(Pile destPile) {
        this.getContainingPile().getCards().remove(this);
        destPile.addCard(this);
    }

    public void flip() {
        faceDown = !faceDown;
        setImage(faceDown ? backFace : frontFace);
    }

    @Override
    public String toString() {
        return "the " + "Rank " + rank + " of " + "Suit " + suit;
    }

    public static boolean isOppositeColor(Card card1, Card card2) {
        return (!(card1.getCardColor().equals(card2.getCardColor())));
    }

    public static boolean isSameSuit(Card card1, Card card2) {
        return card1.getSuit() == card2.getSuit();
    }

    public static List<Card> createNewDeck() {
        List<Card> result = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                result.add(new Card(suit, rank, true));
            }
        }
        return result;
    }

    public static void loadCardImages() {
        cardBackImage = new Image("card_images/card_back.png");
        String suitName = "";
        for (Suit suit : Suit.values()) {
            switch (suit) {
                case HEARTS:
                    suitName = "hearts";
                    break;
                case DIAMONDS:
                    suitName = "diamonds";
                    break;
                case SPADES:
                    suitName = "spades";
                    break;
                case CLUBS:
                    suitName = "clubs";
                    break;
            }
            for (Rank rank : Rank.values()) {
                String cardName = suitName + rank.value;
                String cardId = "S" + suit + "R" + rank;
                String imageFileName = "card_images/" + cardName + ".png";
                cardFaceImages.put(cardId, new Image(imageFileName));
            }
        }
    }

    private String getCardColor() {
        switch (suit) {
            case HEARTS:
                return "red";
            case DIAMONDS:
                return "red";
            case SPADES:
                return "black";
            case CLUBS:
                return "black";
            }
        return "";
    }

    public static boolean isAdjacent(Card draggedCard, Card topCard, Pile.PileType pileType) {
        boolean adjacentFlag = false;
        switch (pileType) {
            case FOUNDATION:
                if (draggedCard.getRank().value == topCard.getRank().value + 1) {
                    adjacentFlag = true;
                }
                break;
            case TABLEAU:
                if (draggedCard.getRank().value == topCard.getRank().value - 1) {
                    adjacentFlag = true;
                }
                break;
        }
        return adjacentFlag;
    }

    public boolean isHighestRank(Pile.PileType pileType) {
        switch (pileType) {
            case TABLEAU:
                if (this.getRank() == Rank.KING) {
                    return true;
                }
                break;
            case FOUNDATION:
                if (this.getRank() == Rank.ACE) {
                    return true;
                }
                break;
        }
        return false;
    }

    public enum Rank {
        ACE (1),
        DEUCE (2),
        THREE (3),
        FOUR (4),
        FIVE (5),
        SIX (6),
        SEVEN (7),
        EIGHT (8),
        NINE (9),
        TEN (10),
        JACK (11),
        QUEEN (12),
        KING (13);

        private final int value;

        Rank (int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    public enum Suit {
        HEARTS,
        DIAMONDS,
        SPADES,
        CLUBS
    }

}
