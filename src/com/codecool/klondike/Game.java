package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    public List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;
    private boolean isDraggedCardsFull = false;
    public static Game game;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        isDraggedCardsFull = false;
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.STOCK)
            return;
        if (card.isFaceDown()) {
            return;
        }
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;
        boolean groupCards = false;
        if (!isDraggedCardsFull) {
            for (Card actualCard : activePile.getCards()) {
                if (card.equals(actualCard)) {
                    groupCards = true;
                }
                if (groupCards) {
                    draggedCards.add(actualCard);
                }
            }
            isDraggedCardsFull = true;
        }
        for (Card actualCard : draggedCards) {
            actualCard.getDropShadow().setRadius(20);
            actualCard.getDropShadow().setOffsetX(10);
            actualCard.getDropShadow().setOffsetY(10);

            actualCard.toFront();
            actualCard.setTranslateX(offsetX);
            actualCard.setTranslateY(offsetY);
        }
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        Pile pile;
        List<Pile> allPiles = new ArrayList<>();
        allPiles.addAll(foundationPiles);
        allPiles.addAll(tableauPiles);
        pile = getValidIntersectingPile(card, allPiles);
        if (pile != null) {
            handleValidMove(card, pile);

        } else {
            draggedCards.forEach(MouseUtil::slideBack);
        }
    };

    public boolean isGameWon() {
        int foundationCards = 0;
        for (Pile pile : foundationPiles) {
            foundationCards += pile.numOfCards();
        }
        return (foundationCards == 52);
    }

    public Game() {
        game = this;
        deck = Card.createNewDeck();
        initBoard();
    }

    private void flipCardsDown(List<Card> deck) {
        for (Card card : deck) {
            if (!card.isFaceDown()) {
                card.flip();
            }
        }
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        discardPile.reverseOrderOfCards();
        for (Card card : discardPile.getCards()) {
            card.flip();
            stockPile.addCard(card);
        }
        discardPile.clear();
        System.out.println("Stock refilled from discard pile.");
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        Card topCard = destPile.getTopCard();
        Pile.PileType pileType = destPile.getPileType();
        if (destPile.isEmpty()) {
            return (card.isHighestRank(pileType));
        } else {
            if (pileType == Pile.PileType.TABLEAU) {
                return (Card.isOppositeColor(card, topCard) && Card.isAdjacent(card, topCard, pileType));
            } else if (pileType == Pile.PileType.FOUNDATION) {
                if (draggedCards.size() == 1) {
                    return (Card.isSameSuit(card, topCard) && Card.isAdjacent(card, topCard, pileType));
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
    }

    public void checkWin() {
        if (isGameWon()) {
            AlertBox.display("Winner", "Congratulations! You have won!");
        }
    }

    private void initBoard() {
        getChildren().clear();
        Collections.shuffle(deck);
        flipCardsDown(deck);
        initPiles();
        dealCards();
        initButtons();
        demoButton();
    }

    private void initButtons() {
        Button restartBtn = new Button();
        restartBtn.setText("Restart");
        restartBtn.setLayoutX(5);
        restartBtn.setLayoutY(5);
        getChildren().add(restartBtn);
        restartBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                for (Pile pile : foundationPiles) {
                    pile.clear();
                }
                for (Pile pile : tableauPiles) {
                    pile.clear();
                }
                tableauPiles.clear();
                foundationPiles.clear();
                stockPile.clear();
                discardPile.clear();
                initBoard();
            }
        });
    }

    private void demoButton() {
        Button demoBtn = new Button();
        demoBtn.setText("Demo");
        demoBtn.setLayoutX(5);
        demoBtn.setLayoutY(40);
        getChildren().add(demoBtn);

        demoBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                for (Pile pile : foundationPiles) {
                    pile.clear();
                }
                for (Pile pile : tableauPiles) {
                    pile.clear();
                }
                tableauPiles.clear();
                foundationPiles.clear();
                stockPile.clear();
                discardPile.clear();
                setDemoBoard();
            }
        });
    }

    private void setDemoBoard() {
        getChildren().clear();
        deck = Card.createNewDeck();
        flipCardsDown(deck);
        initPiles();
        dealDemoCards();
        initButtons();
        demoButton();
    }

    private void dealDemoCards() {
        Iterator<Card> deckIterator = deck.iterator();

        while (deckIterator.hasNext()) {

            for (int i = 0; i < foundationPiles.size() - 1; i++) {
                Pile currentPile = foundationPiles.get(i);
                for (int j = 0; j < 13; j++) {
                    Card currentCard = deckIterator.next();
                    currentCard.flip();
                    currentPile.addCard(currentCard);
                    addMouseEventHandlers(currentCard);
                    getChildren().add(currentCard);
                }
            }

            Pile currentPile = foundationPiles.get(3);
            for (int j = 0; j < 12; j++) {
                Card currentCard = deckIterator.next();
                currentCard.flip();
                currentPile.addCard(currentCard);
                addMouseEventHandlers(currentCard);
                getChildren().add(currentCard);
            }

            deckIterator.forEachRemaining(card -> {
                tableauPiles.get(0).addCard(card);
                card.flip();
                addMouseEventHandlers(card);
                getChildren().add(card);
            });

        }
    }

    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        while (deckIterator.hasNext()) {
            for (int i = 0; i < tableauPiles.size(); i++) {
                Pile currentPile = tableauPiles.get(i);
                for (int j = 0; j < i + 1; j++) {
                    Card currentCard = deckIterator.next();
                    currentPile.addCard(currentCard);
                    addMouseEventHandlers(currentCard);
                    getChildren().add(currentCard);
                }
                Card topCard = currentPile.getTopCard();
                topCard.flip();
            }

            deckIterator.forEachRemaining(card -> {
                stockPile.addCard(card);
                addMouseEventHandlers(card);
                getChildren().add(card);
            });
        }
        for (int i = 0; i < tableauPiles.size(); i++) {
            Pile currentPile = tableauPiles.get(i);
            currentPile.addChangeListener();
        }
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
