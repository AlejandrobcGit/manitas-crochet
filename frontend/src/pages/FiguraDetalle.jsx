import { useParams } from "react-router-dom";

function FiguraDetalle() {

    const { id } = useParams();

    return (
        <>
            <h1>Detalle figura</h1>
            <p>Id: {id}</p>
          </>
    );
}

export default FiguraDetalle;