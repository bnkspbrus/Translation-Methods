package gen;

public class Table {
    public static final Cell[][] TABLE = new Cell[][] {
//                               STAR                         PLUS                     OBRACKET                     CBRACKET                       NUMBER                            $                        start                            t                            f
        {                        null,                        null,   new Cell(Action.SHIFT, 1),                        null,   new Cell(Action.SHIFT, 2),                        null,  new Cell(Action.SHIFT, 11),   new Cell(Action.SHIFT, 3),   new Cell(Action.SHIFT, 6) },
        {                        null,                        null,   new Cell(Action.SHIFT, 1),                        null,   new Cell(Action.SHIFT, 2),                        null,   new Cell(Action.SHIFT, 7),   new Cell(Action.SHIFT, 3),   new Cell(Action.SHIFT, 6) },
        {  new Cell(Action.REDUCE, 5),  new Cell(Action.REDUCE, 5),                        null,  new Cell(Action.REDUCE, 5),                        null,  new Cell(Action.REDUCE, 5),                        null,                        null,                        null },
        {   new Cell(Action.SHIFT, 4),  new Cell(Action.REDUCE, 1),                        null,  new Cell(Action.REDUCE, 1),                        null,  new Cell(Action.REDUCE, 1),                        null,                        null,                        null },
        {                        null,                        null,                        null,                        null,                        null,                        null,                        null,                        null,   new Cell(Action.SHIFT, 5) },
        {  new Cell(Action.REDUCE, 4),  new Cell(Action.REDUCE, 4),                        null,  new Cell(Action.REDUCE, 4),                        null,  new Cell(Action.REDUCE, 4),                        null,                        null,                        null },
        {  new Cell(Action.REDUCE, 3),  new Cell(Action.REDUCE, 3),                        null,  new Cell(Action.REDUCE, 3),                        null,  new Cell(Action.REDUCE, 3),                        null,                        null,                        null },
        {                        null,   new Cell(Action.SHIFT, 8),                        null,  new Cell(Action.SHIFT, 10),                        null,                        null,                        null,                        null,                        null },
        {                        null,                        null,                        null,                        null,                        null,                        null,                        null,   new Cell(Action.SHIFT, 9),                        null },
        {                        null,  new Cell(Action.REDUCE, 2),                        null,  new Cell(Action.REDUCE, 2),                        null,  new Cell(Action.REDUCE, 2),                        null,                        null,                        null },
        {  new Cell(Action.REDUCE, 6),  new Cell(Action.REDUCE, 6),                        null,  new Cell(Action.REDUCE, 6),                        null,  new Cell(Action.REDUCE, 6),                        null,                        null,                        null },
        {                        null,   new Cell(Action.SHIFT, 8),                        null,                        null,                        null,  new Cell(Action.REDUCE, 0),                        null,                        null,                        null }

    };
    public enum Action {
            SHIFT, REDUCE
    }

    public record Cell(Action action, int number) {
    }

}